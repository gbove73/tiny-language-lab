"use client";

import { FormEvent, useEffect, useState } from "react";
import { ProbabilityChart, displayToken } from "@/components/ProbabilityChart";
import { GenerationResult, ModelSnapshot, PredictionView, modelApi } from "@/lib/model-api";

const STARTER_CORPUS = `la luna illumina il lago.
la luce danza lenta sull'acqua.
la notte ascolta il vento e il vento racconta.`;

export default function Home() {
  const [corpus, setCorpus] = useState(STARTER_CORPUS);
  const [order, setOrder] = useState(2);
  const [prompt, setPrompt] = useState("la ");
  const [temperature, setTemperature] = useState(0.7);
  const [newTokens, setNewTokens] = useState(60);
  const [snapshot, setSnapshot] = useState<ModelSnapshot | null>(null);
  const [prediction, setPrediction] = useState<PredictionView | null>(null);
  const [generation, setGeneration] = useState<GenerationResult | null>(null);
  const [busyAction, setBusyAction] = useState<"train" | "generate" | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    modelApi.getModel().then(setSnapshot).catch(() => {
      setError("Backend non raggiungibile. Avvialo con: cd backend && mvn spring-boot:run");
    });
  }, []);

  async function trainModel(event: FormEvent) {
    event.preventDefault();
    setBusyAction("train");
    setError("");
    try {
      const trainedModel = await modelApi.train(corpus, order);
      setSnapshot(trainedModel);
      setGeneration(null);
      setPrediction(await modelApi.predict(prompt));
    } catch (cause) {
      setError(readError(cause));
    } finally {
      setBusyAction(null);
    }
  }

  async function inspectPrediction() {
    setError("");
    try {
      setPrediction(await modelApi.predict(prompt));
    } catch (cause) {
      setError(readError(cause));
    }
  }

  async function generateText() {
    setBusyAction("generate");
    setError("");
    try {
      // Il seed fisso rende l'esperimento ripetibile: stessi dati e controlli, stesso risultato.
      setGeneration(await modelApi.generate(prompt, newTokens, temperature, 42));
    } catch (cause) {
      setError(readError(cause));
    } finally {
      setBusyAction(null);
    }
  }

  return (
    <main>
      <nav className="topbar" aria-label="Intestazione del laboratorio">
        <a className="brand" href="#top" aria-label="Tiny Language Lab, torna all'inizio">
          <span className="brand-mark">TL</span>
          <span>Tiny Language Lab</span>
        </a>
        <div className="runtime-status"><span aria-hidden="true" /> Modello locale</div>
      </nav>

      <header className="hero" id="top">
        <div>
          <span className="eyebrow">Laboratorio 01 · Modelli probabilistici</span>
          <h1>Come una macchina<br />completa il testo</h1>
        </div>
        <p className="lede">
          Un ambiente interattivo per addestrare un modello trasparente, osservare le sue
          probabilità e seguire ogni carattere generato.
        </p>
      </header>

      <aside className="principle" aria-label="Principio del modello">
        <span>Principio</span>
        <code>contesto → distribuzione → campionamento → nuovo contesto</code>
      </aside>

      {error && <div className="error" role="alert">{error}</div>}

      <section className="lesson-grid" aria-label="Esperimento interattivo">
        <article className="panel training-panel">
          <Step number="01" title="Fagli leggere un testo" />
          <p className="explanation">Il corpus è il suo intero mondo. Il modello conterà quali caratteri seguono ogni contesto.</p>
          <form onSubmit={trainModel}>
            <label htmlFor="corpus">Corpus di addestramento</label>
            <textarea id="corpus" value={corpus} onChange={(event) => setCorpus(event.target.value)} rows={8} />
            <div className="control-row">
              <label htmlFor="order">Memoria: <strong>{order} caratteri</strong></label>
              <input id="order" type="range" min="1" max="6" value={order}
                     onChange={(event) => setOrder(Number(event.target.value))} />
            </div>
            <button className="primary" disabled={busyAction !== null} type="submit">
              {busyAction === "train" ? "Sto contando…" : "Addestra il modello"}
            </button>
          </form>
        </article>

        <article className="panel stats-panel">
          <Step number="02" title="Guarda cosa ha imparato" />
          {snapshot ? (
            <>
              <div className="stats">
                <Stat value={snapshot.vocabularySize} label="token distinti" />
                <Stat value={snapshot.learnedContexts} label="contesti appresi" />
                <Stat value={snapshot.observedTransitions} label="transizioni" />
              </div>
              <p className="caption">Vocabolario</p>
              <div className="tokens">
                {snapshot.vocabulary.map((token) => <code key={token}>{displayToken(token)}</code>)}
              </div>
            </>
          ) : <p className="muted">In attesa del backend…</p>}
        </article>

        <article className="panel prediction-panel">
          <Step number="03" title="Interroga le probabilità" />
          <label htmlFor="prompt">Inizio del testo</label>
          <div className="prompt-row">
            <input id="prompt" value={prompt} onChange={(event) => setPrompt(event.target.value)} />
            <button className="secondary" onClick={inspectPrediction}>Analizza</button>
          </div>
          {prediction && (
            <>
              <p className="context-line">Contesto letto: <code>{prediction.context || "∅"}</code></p>
              <ProbabilityChart predictions={prediction.predictions} />
            </>
          )}
        </article>

        <article className="panel generation-panel">
          <Step number="04" title="Lascia che continui" />
          <div className="sliders">
            <label htmlFor="temperature">Temperatura <strong>{temperature.toFixed(1)}</strong>
              <span>Bassa = prudente · Alta = sorprendente</span>
            </label>
            <input id="temperature" type="range" min="0.1" max="2" step="0.1" value={temperature}
                   onChange={(event) => setTemperature(Number(event.target.value))} />
            <label htmlFor="newTokens">Lunghezza <strong>{newTokens}</strong>
              <span>Numero di nuovi caratteri</span>
            </label>
            <input id="newTokens" type="range" min="10" max="200" step="10" value={newTokens}
                   onChange={(event) => setNewTokens(Number(event.target.value))} />
          </div>
          <button className="primary" disabled={busyAction !== null} onClick={generateText}>
            {busyAction === "generate" ? "Sto scegliendo…" : "Genera continuazione"}
          </button>
          {generation && <GenerationTrace result={generation} />}
        </article>
      </section>

      <footer>
        <p><strong>Dal laboratorio agli LLM</strong></p>
        <p>Questo laboratorio usa una tabella di conteggi. Un Transformer impara invece rappresentazioni numeriche e relazioni a lunga distanza, ma risponde alla stessa domanda fondamentale: “dato il contesto, quale token viene dopo?”</p>
      </footer>
    </main>
  );
}

function Step({ number, title }: { number: string; title: string }) {
  return <div className="step"><span>{number}</span><h2>{title}</h2></div>;
}

function Stat({ value, label }: { value: number; label: string }) {
  return <div><strong>{value}</strong><span>{label}</span></div>;
}

function GenerationTrace({ result }: { result: GenerationResult }) {
  return (
    <div className="result">
      <p className="caption">Risultato</p>
      <blockquote>{result.text}</blockquote>
      <details>
        <summary>Apri la traccia delle {result.steps.length} decisioni</summary>
        <ol className="trace">
          {result.steps.map((step) => (
            <li key={step.position}>
              <span>{step.position}</span> dopo <code>{step.context || "∅"}</code> sceglie
              <code>{displayToken(step.selectedToken)}</code> ({(step.probability * 100).toFixed(1)}%)
            </li>
          ))}
        </ol>
      </details>
    </div>
  );
}

function readError(cause: unknown): string {
  return cause instanceof Error ? cause.message : "Si è verificato un errore inatteso.";
}
