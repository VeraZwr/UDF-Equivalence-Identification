
# Initial Looking Around
- Semantic scheint nicht wirklich irgendwo prebuilt installierbar zu sein. Also muss man clonen und bazel build verwenden?
- Paper unterscheided zwischen statischer analyse mit SMT solver und arbeiten mit Programm-Traces mit SMT solver.
- [ ] Sollen wir dann zwei UDFs laufen lassen und dann die Traces mit SMT gleichstellen?
- Wie mappt man SourceCode zu Formeln?
    - Symbolische Execution Engine fährt drüber? Da kommen dann anscheinend Formelbruchstücke raus (Die Repräsentieren ja eine Art Trace)???
        - SMT basierter model Checker: wie kani in rust. Nimm beiden Funktionen und baue testcase:
          ```rust
          fn test(){
              let input = kani::any();
              assert_eq!(fn1(input),fn2(input))
          }
          ```
        - Wenn der Model checker sagt der testcase is valide, dann sind die Funktionen äquivalent.
        - Aber das wird nicht funktionieren, wenn unterschiedliche Programmiersprachen verwendet werden. Bekomme ich also ein Mapping von Arbitrary Language zu einer Common Language, und dann kann in der getestet werden???

- Alternative Idee: Fuzzen mit denselben Inputs.
    - Wenn alle Paths genommen werden und immer dasselbe rauskommt nimm an, dass die Equivalent sind.
    - Wenn nicht alle Paths entdeckt werden können, dann ist die Funktion "zu komplex"
    - Kann man Potentiell im laufenden Betrieb verwenden, ohne explizit rechenleistung zu verschwenden um die äquivalenz zu testen:
        - man instrumentiert die UDFs die unter äquivalenzverdacht stehen bei der Query Execution
        - Es werden die Outputs bei denselben Inputs verglichen.
        - Wenn Code Coverage bei beiden 100% erreicht und beide immernoch dieselben sachen machen, kann man doch annehmen dass die äquivalent sind?
        - Aber: Corner Cases?! die 100% coverage ist schwierig zu erreichen, vorallem wenn durch versch.
          Programmiersprachen auch versch. runtimes mitgetraced werden. z.b. könnte ein nie eintretender
          Integer Overflow handling code dafür sorgen, dass die nicht als äquivalent angesehen werden.
        - Vllt besser: Speichern von "equivalenten Traces".
          Also beide Funktionen werden ausgeführt, man hasht nicht die inputs, sondern die traces der funktionen.
          Wenn beide funktionen dasselbe Ergebniss bei selben Input berechnen, so setzt man die traces gleich.
          Wird dann die eine Funktion erneut aufgerufen und sie berechnet das Ergebnis mit demselben trace, so kann
          man ggf annehmen dass die andere Funktion nun auch dasselbe berechnen würde und das Ergebnis weiterverwenden.



Hi Ankit,

As I have an appointment at Bürgeramt on Monday 10:00, therefore I will likely not be able to attend the 11:00 Meeting from the beginning.
Jennifer from our Group must also stay at home due to a short notice appointment with a craftsman. Therefore our Group would prefer the online Meeting room instead
(Heres the link again: https://bbb.innocampus.tu-berlin.de/rooms/yyr-ena-6o4-og7/join ). I will nevertheless come to your office, possibly with a delay.

I have looked at the resources you gave us and see three main Options for UDF equivalence testing:

- parse the UDF and compare the ASTs for equivalence somehow. Has many open Questions and will likely not be able to correctly identify equivalences between UDFs when they are written sligtly differently.
- SMT based model Checker (similar to how kani in rust works). Basically get a theorem prover to prove this function as infallible:
  ```rust
  fn test(){
  let input = kani::any();
  assert_eq!(fn1(input),fn2(input))
  }
  ```
- If two UDFs produce the same output on the same inputs, and we observe one of them to follow a known execution path, we can assume the other UDF will also return the same value.

If I do not make it to the meeting in time, please write me an answer e-mail instead.

Best Regards,
Jakob Gerhardt


# First Meeting
- want to do short demo paper


# Finding A Scope Meeting

How forced are we to use semantic? Little docs mean a bunch of time is going towards learning the tool in the first place

- is only haskell lib, we dont have haskell experience, and calling from other lang is going to require some setup.
    - some logic is required to map a semantic::Java::Expression to semantic::Python::Expression
    - how to include it into different project? We have little FFI Experience
    - scope is small when using semantic.

- Semantic: language support for many languages (integration) or support to compare functions from different programming languages?

- Different approach: compare byte code insteadt of ASTs ? E.g. use JVM bytecode
    + "easier"


- project setup
    - getting semantic to work
- using semantic to parse UDF
- map AST to Z3 Formulas
- 
