import { useEffect, useState } from "react";

import { getBackendHealth, type HealthResponse } from "./api";
import "./styles.css";

type HealthState =
  | { status: "loading" }
  | { status: "ready"; health: HealthResponse }
  | { status: "error" };

export default function App() {
  const [healthState, setHealthState] = useState<HealthState>({ status: "loading" });

  useEffect(() => {
    let mounted = true;

    getBackendHealth()
      .then((health) => {
        if (mounted) {
          setHealthState({ status: "ready", health });
        }
      })
      .catch(() => {
        if (mounted) {
          setHealthState({ status: "error" });
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  return (
    <main className="min-h-screen bg-white text-slate-950">
      <section className="mx-auto flex min-h-screen w-full max-w-5xl flex-col justify-center px-6 py-16">
        <div className="border-b border-slate-200 pb-8">
          <p className="mb-3 text-sm font-medium text-slate-500">Runnable commerce skeleton</p>
          <h1 className="text-5xl font-semibold tracking-normal">Mini-Mart</h1>
        </div>

        <div className="pt-8 text-sm text-slate-600">
          {healthState.status === "loading" && <p>Checking backend...</p>}
          {healthState.status === "error" && <p>Backend is unavailable</p>}
          {healthState.status === "ready" && (
            <p>
              Backend: {healthState.health.service} is {healthState.health.status}
            </p>
          )}
        </div>
      </section>
    </main>
  );
}
