# Laboratorio 20 aprile 2020
## Parser ricorsivo top-down guidato da una grammatica non ambigua; generazione dell'albero della sintassi astratta (AST)

### Scopo del laboratorio
Questo è il primo dei laboratori direttamente propedeutici al progetto finale.
Svilupperemo assieme l'interprete di un semplice linguaggio di programmazione passando
attraverso le tre fasi illustrate durante le lezioni del primo semestre:
1. analisi lessicale e sintattica: sviluppo di un tokenizer, di un parser e implementazione dell'albero della sintassi astratta (AST);
2. semantica statica: sviluppo di un typechecker (controllo dei tipi) ottenuta tramite una visita dell'AST;
3. semantica dinamica: sviluppo di un interprete (ossia, di una macchina virtuale) in grado di valutare le espressioni ed eseguire le istruzioni
del linguaggio tramite un'opportuna visita dell'AST, ovviamente diversa da quella implementata per il controllo dei tipi.

Il progetto finale che dovrete sviluppare consisterà nell'estensione del linguaggio implementato in questi ultimi laboratori. Le soluzioni proposte
per questi laboratori saranno quindi un buon punto di partenza per l'implementazione del vostro progetto. 

In questo laboratorio ci occuperemo del punto 1 indicato sopra. Come vedremo, sviluppare un tokenizer, un parser e fornire un'implementazione completa del
tipo AST è un compito non banale, anche per un semplice linguaggio. La soluzione di riferimento per questo laboratorio consiste in 712 SLOC (source lines of code, quindi escludendo commenti e linee vuote) distribuite su 39 file diversi! Ovviamente, buona parte del codice sarà già presente, voi dovrete 'solamente'
- comprendere il funzionamento globale del progetto;
- completare alcune classi del progetto; i dettagli si trovano in fondo a questo README.

Per portare a termine il vostro compito con **successo** sarà indispensabile fare un po' di ripasso, in particolare vi consiglio di rivedervi il seguente materiale disponibile su AulaWeb:
- [lezione 09-26: Linguaggi formali e loro operatori. Espressioni regolari](https://2019.aulaweb.unige.it/mod/resource/view.php?id=22690)
- [lezione 09-30: Espressioni regolari: semantica e sintassi concreta](https://2019.aulaweb.unige.it/mod/resource/view.php?id=25932)
- [lezione 10-03: Linguaggi regolari. Analisi sintattica: parser, albero di derivazione, albero della sintassi astratta](https://2019.aulaweb.unige.it/mod/resource/view.php?id=28130)
- [lezione 10-07: Grammatiche context-free. Derivazioni a uno o più passi](https://2019.aulaweb.unige.it/mod/resource/view.php?id=29818)
- [lezione 10-14: Parser top-down ricorsivi](https://2019.aulaweb.unige.it/mod/resource/view.php?id=32660)
- [lezione 12-03: Espressioni regolari in Java](https://2019.aulaweb.unige.it/mod/resource/view.php?id=46895)
- [laboratorio 12-09. Java: espressioni regolari](https://2019.aulaweb.unige.it/mod/folder/view.php?id=47998)

#### Specifica della sintassi del linguaggio
La grammatica EBNF del linguaggio si trova all'inizio del file `lab09_04_20.parser.BufferedParser` sotto forma di commento ed è già in forma non
ambigua;  essa è una guida indispensabile per lo sviluppo del codice della classe
`lab09_04_20.parser.BufferedParser`.
```txt
Prog ::= StmtSeq EOF
StmtSeq ::= Stmt (';' StmtSeq)?
Stmt ::= 'var'? IDENT '=' Exp | 'print' Exp |  'if' '(' Exp ')' '{' StmtSeq '}' ('else' '{' StmtSeq '}')? 
Exp ::= Eq ('&&' Eq)* 
Eq ::= Add ('==' Add)*
Add ::= Mul ('+' Mul)*
Mul::= Atom ('*' Atom)*
Atom ::= '<<' Exp ',' Exp '>>' | 'fst' Atom | 'snd' Atom | '-' Atom | '!' Atom | BOOL | NUM | IDENT | '(' Exp ')'
```
Secondo le convenzioni adottate a lezione, la grammatica si basa su categorie lessicali definite da espressioni regolari nel
tokenizer implementato dalla classe `lab09_04_20.parser.BufferedTokenizer` e dal tipo `enum` 
`lab09_04_20.parser.TokenType` (vedere le [slide associate](https://2019.aulaweb.unige.it/pluginfile.php/241405/mod_folder/content/0/lect34-04-20.pdf?forcedownload=1)): `ID` per gli identificatori di variabile, `BOOL` e `NUM` per i literal di tipo booleano e naturale.
La categoria lessicale `EOF` è speciale ed essenziale per identificare la fine del programma e per garantire che esso non termini con
elementi estranei alla sintassi: nel nostro caso il non terminale principale `Prog` specifica che un programma sintatticamente corretto può solo essere
una sequenza non vuota di istruzioni separati dal terminale `;`.

#### Tokenizer
Il parser  `lab09_04_20.parser.BufferedParser` si basa sulla classe `lab09_04_20.parser.BufferedTokenizer` (che implementa l'interfaccia
`lab09_04_20.parser.Tokenizer`) e sul tipo `enum` che definisce tutti i possibili tipi di token; entrambi i codici che troverete come starter-code sono già completi e funzionanti. La nozione di token è un'astrazione della nozione di lessema
che permette di passare dalla sintassi concreta a quella astratta. Il tipo  `STMT_SEP` rappresenta il token usato per separare due istruzioni;
il parser potrà riferirsi direttamente a esso senza necessariamente sapere che il lessema usato concretamente è la stringa `;`. In questo modo il codice risulterà più leggibile e più facilmente modificabile: nel caso volessimo usare un simbolo diverso, basterà modificare `lab09_04_20.parser.BufferedTokenizer` lasciando invariato il codice del parser contenuto in `lab09_04_20.parser.BufferedParser`. In modo del tutto simile, verranno definiti i tipi di token per le
parole chiave (keyword), ossia quei terminali la cui sintassi corrisponde a quella degli identificatori di variabili, ma che non possono essere utilizzati per identificare una variabile; per esempio `PRINT` corrisponde alla parola chiave che introduce l'istruzione di stampa, ma il parser non deve curarsi della rappresentazione concreta della parola, si essa `print`, `log`, `output` o qualsiasi altra ragionevole scelta.

Come noterete, la parte di definizione  dei token è statica, ossia realizzata tramite variabili di classe, poiché sarebbe poco pratico e non particolarmente
utile creare tokenizer a partire dalla stessa classe in grado di definire token diversi. In particolare le mappe `keywords` e `symbols` associano
alle parole chiavi e ai simboli concreti il loro corrispondente tipo astratto; ricordatevi che un simbolo ha una sintassi concreta che non può essere confusa
con quella di un identificatore, mentre una keyword non può essere usata come identificatore, anche se la sua sintassi sarebbe compatibile; vedere per esempio il programma `tests/failure/prog09.txt`. I tipi `SKIP`, `IDENT` e `NUM` corrispondono a insiemi di lessemi definiti da espressioni regolari individuate da gruppi tramite l'uso delle parentesi tonde; per comodità, tali gruppi coincidono con l'ordine in cui `SKIP`, `IDENT` e `NUM` sono elencati nel
tipo `enum` `lab09_04_20.parser.TokenType`. Questa scelta giustifica l'uso del metodo predefinito `ordinal()` nel metodo `assignTokenType()` della
classe `lab09_04_20.parser.BufferedTokenizer`. Il tipo `SKIP` coincide con gli spazi bianchi e i commenti su singola linea, ossia tutti token
che vanno saltati perché non interessano il parser. Infine, il tipo `EOF` è molto speciale e indica il token corrispondente alla fine del programma. 

Il tokenizer `lab09_04_20.parser.BufferedTokenizer` implementa la seguente interfaccia `lab09_04_20.parser.Tokenizer`:
```java
public interface Tokenizer extends AutoCloseable {

	TokenType next() throws TokenizerException;

	TokenType tokenType();

	String tokenString();

	int intValue();

	boolean boolValue();

	public void close() throws IOException;

	int getLineNumber();
}
```
Il metodo principale è `TokenType next()` usato per avanzare nella lettura del buffered reader associato al tokenizer: esso
solleva l'eccezione controllata (checked) `TokenizerException` se non esiste un prossimo valido lessema, altrimenti restituisce
il tipo del token appena riconosciuto che può essere `EOF` se il buffered reader è terminato; il tipo `SKIP` non può essere mai
restituito per le ragioni spiegate precedentemente.

Il metodo `TokenType tokenType()` restituisce il tipo del token appena riconosciuto (coincidente con l'ultimo valore restituito da `next()`)
o solleva l'eccezione `IllegalStateException` se nessun token è stato precedentemente riconosciuto.

Il metodo `String tokenString()` restituisce il lessema corrispondente al token appena riconosciuto o solleva l'eccezione
`IllegalStateException` se nessun token è stato precedentemente riconosciuto.

Il metodo `int intValue()` restituisce il valore del token di tipo `NUM` che è stato appena riconosciuto 
 o solleva l'eccezione `IllegalStateException` se nessun token di tale tipo è stato precedentemente riconosciuto.

Il metodo `boolean boolValue()` restituisce il valore del token di tipo `BOOL` che è stato appena riconosciuto 
 o solleva l'eccezione `IllegalStateException` se nessun token di tale tipo è stato precedentemente riconosciuto.

Il metodo	`void close() throws IOException` è necessario perché i tokenizer sono di tipo `AutoCloseable` per poter essere
gestiti tramite il costrutto `try-with-resources`, dato che decorano dei `BufferedReader`; la stessa considerazione vale
per l'interfaccia `lab09_04_20.parser.Parser`, visto che un parser decora sempre un `Tokenizer` (vedere il costruttore `BufferedParser(BufferedTokenizer)`).

Il metodo `int getLineNumber()` restituisce la linea corrente del `BufferedReader` associato al tokenizer, grazie alla decorazione
`java.io.LineNumberReader` (vedere il costruttore `BufferedTokenizer(BufferedReader)`); questo permette di visualizzare messaggi che facilitino l'individuazione dell'errore.

#### Implementazione dell'albero della sintassi astratta (AST)
Il package `lab09_04_20.parser.ast` contiene tutte le definizioni necessarie all'implementazione dell'albero della sintassi astratta (AST).
L'interfaccia `AST` corrispondente al tipo principale, le sotto-interfacce `Prog`, `StmtSeq`, `Stmt`, `Exp` e `Ident` corrispondono alle categorie sintattiche
principali (ossia i non-terminal principali). Per questo laboratorio le interfacce non devono necessariamente contenere metodi, dato che lo scopo
del progetto è effettuare il parsing di un programma e produrre il suo corrispondente AST; nei prossimi laboratori tale interfacce dovranno essere popolate da metodi corrispondenti alle azioni necessarie su un AST (visita per typechecking, visita per valutazione/esecuzione).

Alcune classi astratte permettono raccogliere a fattore comune codice riutilizzabile:
- `UnaryOp`: codice comune agli operatori unari;
- `BinaryOp`: codice comune agli operatori binari;
- `PrimLiteral<T>`: codice comune alle foglie che corrispondono a literal di tipo primitivo;
- `AbstractAssignStmt`: per gli statement `var` (dichiarazione) e assegnazione che hanno la stessa forma;

Per quanto riguarda le sequenze di un numero variabile di elementi sintattici (come accade per `StmtSeq`), sarebbe possibile implementare
nodi di un AST con un numero variabile di figli. ma per semplicità abbiamo preferito definire sempre nodi con un numero costante di figli.

- `SingleStmt`: sequenza con un unico statement, quindi nodo con un unico figlio di tipo `Stmt`.
- `MoreStmt`: sequenza con almeno due statement, quindi nodo con due figli, il primo di tipo `Stmt`, il secondo di tipo `StmtSeq`.

Entrambe le classi usano le classi generiche astratte `Single<T>` e `More<FT,RT>` che potrebbero essere utili in simili situazioni; per esempio, se
si dovesse estendere il linguaggio con sequenze di espressioni.

##### Importante
Per motivi di testing, tutte le classi che implementano i nodi dell'AST ridefiniscono il metodo `String toString()` ereditato da `Object` per visualizzare
il corrispondente AST in forma linearizzata mediante un termine. Per tale scopo vengono usati i metodi predefiniti `getClass()`
e `getSimpleName()` che permettono di accedere al nome della classe di un oggetto.
Per esempio, la stampa dell'AST generato dal parser a partire dal programma contenuto nel file ` tests/success/prog01.txt` produce il termine
```java
ProgClass(MoreStmt(PrintStmt(Add(Sign(IntLiteral(40)),Mul(IntLiteral(5),IntLiteral(3)))),SingleStmt(PrintStmt(Sign(Mul(Add(IntLiteral(40),IntLiteral(5)),IntLiteral(3)))))))
```

### Classi da completare

#### Package `lab09_04_20.parser`
L'unica classe da completare è `BufferedParser`

#### Package `lab09_04_20.parser.ast`

- `AssignStmt`
- `VarStmt`
- `IfStmt`
- `Add`
- `Fst`

### Tests
Potete utilizzare i test nei seguenti folder
- `tests/success`: programmi corretti sintatticamente
- `tests/failure`: programmi con errori di sintassi

