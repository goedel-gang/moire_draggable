import sys
from collections import Counter

"""
my python script to compress a postscript file. it isn't guaranteed to work,
but i've had success using it on my style of postscript code. it doesn't
perform any proper parsing, so is quite shaky in terms of what it can
successfully work on. it works by leaving any special looking comments well
alone, but parsing the postscript, removing comments, removing unused
functions, and then performing some pretty brutal mangling.
"""

#legal characters for a postscript variable name. feel free to add more if you
#can think of any.
ps_alphabet_chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-+\"£$&*.,@#'|!"
#it's actually a little more complicated but for simplicity i'm just letting
#the second character onwards include numeric digits
secondary = ps_alphabet_chars + "1234567890"

#check for double characters
if len(set(secondary)) != len(secondary):
    raise ValueError("ps alphabet has double characters")

def to_base(base, num):
    """
    convert to base, using a beautifully compact little recursive generator
    eventhoughisaysomyself
    """
    quot, rem = divmod(num, base)
    yield rem
    if quot:
        yield from to_base(base, quot)

def generate_var_names():
    """
    a generator that methodically goes over possible mangled variable names
    """
    curr_name_val = 0
    while True:
        digits = to_base(len(ps_alphabet_chars), curr_name_val)
        yield "".join((ps_alphabet_chars if ind == 0 else secondary)[i] for ind, i in enumerate(digits))
        curr_name_val += 1

def join_curlies(components):
    """
    join a list of strings, with a little extra logic meaning that no
    whitespace is used at the edges of functions
    """
    out = []
    prec_space = False
    for i in components:
        if i == "}":
            out.append(i)
            prec_space = True
        else:
            if prec_space:
                out.append(" ")
            out.append(i)
            prec_space = i != "{"
    return "".join(out)

class PostScriptParser:
    """
    a class to contain my pseudoparsed postscript and process it
    """
    def __init__(self, ps):
        self.setup = []
        self.components = []
        self.teardown = []
        self.read_ps(ps)
        self.vargenerator = generate_var_names()
        self.varmap = {}

    #read postscript from an iterable of lines
    def read_ps(self, ps):
        for line in ps:
            line = line.strip()
            #special ending type line
            if line == "%%Trailer" or line == "%%EOF":
                self.teardown.append(line)
            #special starting lines
            elif line.startswith("%%") or line.startswith("%!") or line.startswith("<<"):
                self.setup.append(line)
            #comments are stored as a whole entity
            elif line.startswith("%"):
                self.components.append(line)
            elif "%" in line:
                loc = line.index("%")
                self.components.extend(line[:loc].split())
                self.components.append(line[loc:])
            #otherwise "parse" the line by splitting it by whitespace
            else:
                self.components.extend(line.split())

    #remove all comments from parsed tokens (iterating backwards as things are being removed)
    def remove_comments(self):
        for ind, i in reversed(list(enumerate(self.components))):
            if i.startswith("%"):
                del self.components[ind]

    #remove unused functions. this is quite a piece of work - it keeps passing
    #through all parsed components until nothing is left to be removed. This
    #pattern allows chains of unused functions to be untangled
    def comb_funcs(self):
        while True:
            for ind, i in enumerate(self.components):
                #looks for a name
                if i.startswith("/"):
                    #if the name never occurs in components and it's a function definition
                    if i[1:] not in self.components and self.components[ind + 1] == "{":
                        #moves through the file until the number of opening and
                        #closing curly brackets is equal. This happens very
                        #naively but works if you've written the postscript
                        #reasonably simply
                        pointer = 1
                        counter = 1
                        while counter:
                            pointer += 1
                            if self.components[ind + pointer] == "{":
                                counter += 1
                            elif self.components[ind + pointer] == "}":
                                counter -= 1

                        #another weakness - it assumes it must be a definition.
                        while self.components[ind + pointer] not in {"def", "binddef"}:
                            pointer += 1

                        del self.components[ind:ind + pointer + 1]
                        break
            else:
                break

    #reassign each variable to a mangled name
    def mangle_vars(self):
        for ind, i in enumerate(self.components):
            if i.startswith("/"):
                name = i[1:]
                if name not in self.varmap:
                    self.varmap[name] = next(self.vargenerator)

                self.components[ind] = "/" + self.varmap[name]
            elif i in self.varmap:
                self.components[ind] = self.varmap[i]

    #reassign each default function to a mangled name. Some special functions
    #are also defined - binddef and exchdef combine these commonly used
    #operations
    def mangle_defaults(self):
        self.varmap["bind"] = next(self.vargenerator)
        self.varmap["def"] = next(self.vargenerator)
        self.varmap["binddef"] = next(self.vargenerator)
        self.varmap["exch"] = next(self.vargenerator)
        self.varmap["exchdef"] = next(self.vargenerator)
        #a lovely piece of work defining some defaults to do with definition
        predefs = "/{0} {{ bind }} bind def /{1} {{ def }} {0} def /{2} {{ {0} {1} }} {0} {1} /{3} {{ exch }} {2} /{4} {{ {3} {1} }} {2}".format(self.varmap["bind"], self.varmap["def"], self.varmap["binddef"], self.varmap["exch"], self.varmap["exchdef"]).split()
        for ind, i in enumerate(self.components):
            if i.isalpha() and i not in self.varmap.values():
                if i not in self.varmap:
                    self.varmap[i] = next(self.vargenerator)
                    predefs.extend("/{} {{{}}} {}".format(self.varmap[i], i, self.varmap["binddef"]).split())
                self.components[ind] = self.varmap[i]

        self.components = predefs + self.components

    #this remangled all mangled names, assigning shorted names to more frequent
    #variables. This hasn't actually come in useful yet as there are few enough
    #variables to store with one character each, but one day it might
    def remangle(self):
        count = Counter((i[1:] if i.startswith("/") else i) for i in self.components if (i[1:] if i.startswith("/") else i) in self.varmap.values()).most_common()
        newgen = generate_var_names()
        newmap = {i[0]: next(newgen) for i in count}
        for ind, i in enumerate(self.components):
            if i in newmap:
                self.components[ind] = newmap[i]
            elif i.startswith("/"):
                self.components[ind] = "/" + newmap[i[1:]]

    #regenerate into postscript
    def get_ps(self):
        return "{}\n{}\n{}".format("\n".join(self.setup), join_curlies(self.components), "\n".join(self.teardown))

    #get string representation
    def __str__(self):
        return "{0.setup}\n{0.components}\n{0.teardown}".format(self)

def compress_ps(ps):
    """
    apply a PostScriptParser to a postscript plaintext and compress in every
    way conceivable
    """
    parser = PostScriptParser(ps.replace("{", " { ").replace("}", " } ").replace("bind def", "binddef").replace("exch def", "exchdef").split("\n"))
    parser.remove_comments()
    parser.comb_funcs()
    parser.mangle_vars()
    parser.mangle_defaults()
    parser.remangle()
    return parser.get_ps()

def main():
    ps = compress_ps(sys.stdin.readlines())
    print(ps)

if __name__ == "__main__":
    main()
