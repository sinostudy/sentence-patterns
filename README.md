# study.sino.sentence-patterns
> _**WARNING:** DO NOT USE! UNDER CONSTRUCTION!_

This library is a long and arduous journey towards achieving automated detection of a wide variety of sentence patterns in Chinese text. Chinese text is turned into [dependency grammar](https://en.wikipedia.org/wiki/Dependency_grammar) graphs using [datalinguist](https://github.com/simongray/datalinguist). The [Semgrex](https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/semgraph/semgrex/SemgrexPattern.html) DSL is then used to locate instances of known sentence patterns within these graphs.

## Rationale
While the student of the Chinese language can look up individual _words_ in a dictionary, various common patterns found in Chinese sentences will at times make this a more difficult endeavour than it should be. That is because many sentence patterns slightly _shift_ the meaning of certain key words in the sentence, unbeknownst to the student.

What the student seemingly needs is _another_ dictionary, this one to look up Chinese sentence patterns (these books do exist). However, sentence patterns are much more dynamic than the blocks of Chinese characters that we typically call "words" and their entries unfortunately can't be found as easily. Luckily, these days we can use [NLP](https://en.wikipedia.org/wiki/Natural_language_processing) to automate much of the grammatical analysis required to locate sentence patterns; that is the point of this library.
