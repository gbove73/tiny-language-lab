import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ProbabilityChart, displayToken } from "./ProbabilityChart";

describe("ProbabilityChart", () => {
  it("shows tokens, percentages and observed counts", () => {
    render(<ProbabilityChart predictions={[{ token: "a", probability: 0.75, observedCount: 3 }]} />);

    expect(screen.getByText("a")).toBeInTheDocument();
    expect(screen.getByText("75.0%")).toBeInTheDocument();
    expect(screen.getByText("3× visto")).toBeInTheDocument();
  });

  it("makes whitespace visible", () => {
    expect(displayToken(" ")).toBe("␠");
    expect(displayToken("\n")).toBe("↵");
  });
});
