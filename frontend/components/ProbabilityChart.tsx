import type { TokenProbability } from "@/lib/model-api";

type ProbabilityChartProps = { predictions: TokenProbability[] };

/** Rende visibile la distribuzione: la barra più lunga è il prossimo token favorito. */
export function ProbabilityChart({ predictions }: ProbabilityChartProps) {
  const largestProbability = predictions[0]?.probability ?? 1;

  return (
    <div className="probability-list" aria-label="Probabilità dei prossimi token">
      {predictions.map((prediction) => (
        <div className="probability-row" key={prediction.token}>
          <code>{displayToken(prediction.token)}</code>
          <div className="bar-track">
            <div
              className="bar-fill"
              style={{ width: `${(prediction.probability / largestProbability) * 100}%` }}
            />
          </div>
          <span>{(prediction.probability * 100).toFixed(1)}%</span>
          <small>{prediction.observedCount}× visto</small>
        </div>
      ))}
    </div>
  );
}

export function displayToken(token: string): string {
  if (token === " ") return "␠";
  if (token === "\n") return "↵";
  if (token === "\t") return "⇥";
  return token;
}
