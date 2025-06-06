https://github.com/antlr/antlr4 as semantic
https://github.com/antlr/grammars-v4






In Scope:
- Java first, Python as well
- arithmetic operations (operator overloading breaks this completely? Like what happens on += assignments? )
- if else
- switch case
- finite loops while for
- Types: Numbers (Floats?)
- creating testcases
- documentation:
  presentations
  report

Maybe In-Scope:
-function calls?
- Types: String
- imports 
  - Z3 can work with "black box functions", imports could be represented as such.


Out-Of-scope:
infinite loops
recursion


Approaches:
- AST via semantic -> Z3
- AST via Antlr4 -> Z3
- bytecode magic -> Z3

Technical Decisions:
- 08052024 we use Z3, it was suggested to us, we assume it should work. (but we dont really know yet :) )

Whats Next?
Which Approach/Tools?
- look into semantic (Jenny)
- look into antlr4 (sohaila)
- look into Z3 (weiran)
- look at bytecode (Jakob)
  Questions for antlr4 and semantic and Bytecode:
- can they output ASTs in a format we can use?/how to integrate into a project? include sample project setup
  Questions for Z3:
  - for the in-scope control flow things, draft a plan/theory how these can be encoded in Z3