# Tiny Language Lab

Un laboratorio didattico full stack che spiega il principio fondamentale dei modelli
linguistici: osservare un testo, stimare quale token sia probabile dopo un contesto e
campionare ripetutamente il token successivo.

> **Nota importante:** questo progetto non è un LLM industriale. È un piccolo modello
> n-grammi a caratteri, intenzionalmente trasparente. Gli LLM moderni usano tokenizzatori,
> embedding e Transformer con miliardi di parametri; il ciclo concettuale
> `contesto → probabilità → token successivo` rimane però lo stesso.

## Perché esiste questo progetto

Quando si usa un assistente basato su un LLM, il risultato può sembrare prodotto da una
scatola nera: si scrive un prompt e compare una risposta. Tra questi due momenti, però,
il modello esegue molte volte un'operazione precisa: legge il contesto disponibile,
assegna una probabilità ai token che potrebbero seguirlo, ne sceglie uno e lo aggiunge
al contesto.

Tiny Language Lab rende osservabile questo ciclo. Il suo scopo non è produrre testi di
qualità paragonabile a quelli di un servizio commerciale, ma offrire un programma
abbastanza piccolo da poter essere letto interamente, modificato e verificato. È quindi
pensato per chi vuole:

- capire che un modello linguistico non recupera una frase già pronta, ma costruisce il
  risultato un token alla volta;
- vedere come i dati di addestramento influenzano direttamente le previsioni;
- distinguere una distribuzione di probabilità dalla scelta casuale effettuata su di essa;
- sperimentare con memoria, temperatura, corpus e seed;
- seguire il percorso completo dal calcolo Java fino alla visualizzazione Next.js;
- avere una base semplice sulla quale introdurre in seguito embedding, reti neurali e
  Transformer.

La semplicità è quindi un requisito didattico, non un limite accidentale: ogni passaggio
importante è espresso nel codice anziché nascosto dietro una libreria di machine learning.

## L'idea fondamentale

Il modello è un **n-gramma a caratteri**. Un n-gramma osserva una sequenza di elementi
vicini; in questo progetto gli elementi, chiamati *token*, sono singoli caratteri Unicode.
La quantità di caratteri ricordati prende il nome di `order`, o ordine del modello.

Con il corpus seguente e ordine 2:

```text
casa canta
```

il modello incontra, tra le altre, queste transizioni:

```text
"ca" → "s"
"as" → "a"
"sa" → " "
" c" → "a"
"ca" → "n"
```

Il contesto `"ca"` è apparso due volte: una volta seguito da `"s"` e una volta da
`"n"`. Il modello ha quindi una ragione statistica per considerare entrambi i caratteri
come possibili continuazioni. Non conosce il significato di “casa” o “canta”: conosce
soltanto le regolarità locali osservate nel corpus.

## Dal testo alla generazione, passo per passo

### 1. Corpus

Il corpus è il testo usato come esempio durante l'addestramento. In questo laboratorio
non viene scaricato alcun dato esterno: il modello impara esclusivamente dal testo
inserito nell'interfaccia. Un corpus più lungo offre più esempi; un corpus ripetitivo
rende anche il modello più ripetitivo.

### 2. Tokenizzazione

`CharacterTokenizer` divide il corpus in caratteri Unicode. Anche spazi, punteggiatura,
a capo ed emoji sono token. Un LLM moderno usa normalmente frammenti di parole perché
sono più efficienti, ma i caratteri permettono di vedere chiaramente cosa entra e cosa
esce dal modello.

### 3. Apprendimento dei conteggi

`NGramLanguageModel.train` fa scorrere una finestra sul corpus. Per ogni posizione
registra quante volte un determinato token compare dopo il contesto corrente. Non ci
sono pesi neurali né ottimizzazione iterativa: “addestrare” significa costruire questa
tabella di frequenze.

### 4. Dalle frequenze alle probabilità

`NGramLanguageModel.predict` cerca il contesto nella tabella e normalizza i conteggi.
Il modello applica lo **smoothing di Laplace**, aggiungendo virtualmente 1 al conteggio
di ogni token del vocabolario:

```text
P(token | contesto) = (conteggio osservato + 1)
                       / (totale osservazioni + dimensione vocabolario)
```

In questo modo una transizione mai osservata mantiene una probabilità piccola ma non
nulla. La somma delle probabilità di tutti i token resta uguale a 1.

### 5. Temperatura

Prima della scelta, la temperatura modifica la forma della distribuzione:

- una temperatura bassa accentua le differenze e favorisce le continuazioni più probabili;
- una temperatura alta avvicina le probabilità e aumenta la varietà;
- la temperatura non aggiunge conoscenza: cambia soltanto il modo in cui il modello usa
  ciò che ha imparato.

### 6. Campionamento e ciclo autoregressivo

`NGramLanguageModel.generate` estrae un token dalla distribuzione, lo aggiunge al testo
e usa il nuovo testo come contesto del passo successivo. Questo comportamento si chiama
**autoregressivo**. L'operazione viene ripetuta fino a raggiungere il numero richiesto di
nuovi token.

Il seed inizializza il generatore casuale. Usando lo stesso corpus, ordine, prompt,
temperatura e seed si ottiene lo stesso esperimento: una proprietà molto utile quando si
studia o si testa il comportamento del modello.

## Cosa osservare nell'interfaccia

Il laboratorio propone quattro passaggi corrispondenti al flusso del programma:

1. **Addestramento:** inserire il corpus e scegliere quanti caratteri costituiscono la
   memoria del modello.
2. **Ispezione:** osservare vocabolario, contesti appresi e transizioni registrate.
3. **Previsione:** scrivere un prompt e confrontare le probabilità dei token successivi.
4. **Generazione:** modificare temperatura e lunghezza, poi aprire la traccia per vedere
   contesto, token scelto e probabilità a ogni iterazione.

Alcuni esperimenti utili:

- addestrare il modello su una frase molto ripetitiva e poi su un testo più vario;
- confrontare ordine 1 e ordine 4 usando lo stesso corpus;
- generare più volte senza cambiare i controlli e verificare il ruolo del seed fisso;
- usare un prompt il cui contesto non compare nel corpus e osservare lo smoothing;
- confrontare temperature molto basse e molto alte.

## Somiglianze e differenze rispetto a un LLM moderno

Tiny Language Lab conserva alcuni concetti autentici dei modelli generativi:

- rappresenta il testo come una sequenza di token;
- stima una distribuzione condizionata dal contesto;
- genera in modo autoregressivo;
- usa temperatura e campionamento;
- dipende dai dati sui quali è stato addestrato.

Semplifica invece deliberatamente quasi tutto il resto:

| Tiny Language Lab | LLM moderno |
| --- | --- |
| Singoli caratteri Unicode | Token composti spesso da frammenti di parola |
| Conteggi espliciti in una tabella | Parametri appresi in una rete neurale |
| Contesto da 1 a 8 caratteri | Contesto di migliaia o milioni di token |
| Relazioni locali esatte | Rappresentazioni semantiche distribuite |
| Corpus inserito dall'utente | Dataset molto grandi e diversificati |
| Addestramento immediato sulla CPU | Addestramento distribuito e molto costoso |

Per questo il progetto insegna il **meccanismo di base** della previsione del prossimo
token, ma non simula comprensione, ragionamento o conoscenza generale. Chiamarlo “LLM”
in senso tecnico sarebbe improprio: è il gradino osservabile che prepara a studiarli.

## Architettura

- `backend`: Java 21 e Spring Boot; addestra e interroga il modello in memoria.
- `frontend`: Next.js, React e TypeScript; offre il laboratorio interattivo.

Il backend conserva in memoria un solo modello alla volta. Un nuovo addestramento crea
prima una nuova istanza completa e poi la sostituisce atomicamente, così le richieste non
osservano mai un modello costruito solo in parte. I dati non vengono salvati su disco:
al riavvio torna disponibile il corpus dimostrativo incorporato.

## Avvio

Servono Java 21+, Maven 3.9+, npm 10+ e una versione LTS di Node.js
(20.19+, 22.13+ oppure 24+). Le release dispari di Node.js non sono consigliate.

```bash
# terminale 1
cd backend
mvn spring-boot:run

# terminale 2
cd frontend
npm install
npm run dev
```

Aprire <http://localhost:3000>. Il frontend contatta l'API su
`http://localhost:8080`; la variabile opzionale `NEXT_PUBLIC_API_URL` permette di
cambiare indirizzo.

## Verifica

```bash
cd backend && mvn test
cd frontend && npm run lint && npm test && npm run build
```

## Percorso di lettura consigliato

1. `CharacterTokenizer`: trasforma il testo in token.
2. `NGramLanguageModel.train`: conta le transizioni.
3. `NGramLanguageModel.predict`: normalizza i conteggi in probabilità.
4. `NGramLanguageModel.generate`: ripete previsione e campionamento.
5. `LanguageModelController`: espone queste operazioni via HTTP.
