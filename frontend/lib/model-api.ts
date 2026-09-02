export type ModelSnapshot = {
  order: number;
  vocabularySize: number;
  learnedContexts: number;
  observedTransitions: number;
  vocabulary: string[];
};

export type TokenProbability = {
  token: string;
  probability: number;
  observedCount: number;
};

export type PredictionView = {
  context: string;
  predictions: TokenProbability[];
};

export type GenerationResult = {
  text: string;
  steps: Array<{ position: number; context: string; selectedToken: string; probability: number }>;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Racchiude fetch in un solo punto: componenti e pagine non devono conoscere dettagli HTTP.
 * Gli errori dell'API vengono tradotti in messaggi leggibili per l'interfaccia.
 */
async function request<T>(path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    method: body === undefined ? "GET" : "POST",
    headers: body === undefined ? undefined : { "Content-Type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!response.ok) {
    const problem = (await response.json().catch(() => null)) as { message?: string } | null;
    throw new Error(problem?.message ?? "Il backend non ha completato la richiesta.");
  }
  return response.json() as Promise<T>;
}

export const modelApi = {
  getModel: () => request<ModelSnapshot>("/api/model"),
  train: (corpus: string, order: number) =>
    request<ModelSnapshot>("/api/model/train", { corpus, order }),
  predict: (prompt: string, limit = 8) =>
    request<PredictionView>("/api/model/predict", { prompt, limit }),
  generate: (prompt: string, maxNewTokens: number, temperature: number, seed: number) =>
    request<GenerationResult>("/api/model/generate", { prompt, maxNewTokens, temperature, seed }),
};
