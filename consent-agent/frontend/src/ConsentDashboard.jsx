import React, { useState, useEffect } from "react";

const API_BASE = "http://localhost:8080/api/consents";

const SEVERITY_COLOR = {
  HIGH: "#D85A30",
  MEDIUM: "#EF9F27",
  LOW: "#1D9E75",
};

export default function ConsentDashboard() {
  const [consents, setConsents] = useState([]);
  const [flags, setFlags] = useState([]);
  const [scanning, setScanning] = useState(false);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    fetch(API_BASE).then((r) => r.json()).then(setConsents);
  }, []);

  const runScan = async () => {
    setScanning(true);
    const res = await fetch(`${API_BASE}/scan`, { method: "POST" });
    const data = await res.json();
    setFlags(data);
    setScanning(false);
  };

  const flagsFor = (consentId) => flags.filter((f) => f.consentId === consentId);

  return (
    <div style={{ maxWidth: 900, margin: "0 auto", padding: "24px", fontFamily: "sans-serif" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
        <h1 style={{ fontSize: 22, fontWeight: 500 }}>Consent Intelligence Agent</h1>
        <button
          onClick={runScan}
          disabled={scanning}
          style={{
            padding: "10px 18px", borderRadius: 8, border: "none",
            background: "#534AB7", color: "white", cursor: "pointer", fontSize: 14,
          }}
        >
          {scanning ? "Scanning..." : "Run agent scan"}
        </button>
      </div>
      <p style={{ color: "#5F5E5A", fontSize: 13, marginBottom: 24 }}>
        Runs entirely on-premise via a local model — no customer data leaves this environment.
      </p>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 16 }}>
        {consents.map((c) => {
          const cFlags = flagsFor(c.consentId);
          const worst = cFlags.reduce((acc, f) => {
            const order = { HIGH: 3, MEDIUM: 2, LOW: 1 };
            return order[f.severity] > (order[acc] || 0) ? f.severity : acc;
          }, null);
          const borderColor = worst ? SEVERITY_COLOR[worst] : "#B4B2A9";

          return (
            <div
              key={c.consentId}
              onClick={() => cFlags.length && setSelected(cFlags)}
              style={{
                border: `1px solid ${borderColor}`, borderRadius: 12, padding: 16,
                cursor: cFlags.length ? "pointer" : "default", background: "#fff",
              }}
            >
              <div style={{ fontWeight: 500, fontSize: 15 }}>{c.tppName}</div>
              <div style={{ fontSize: 12, color: "#5F5E5A", marginTop: 4 }}>{c.scope}</div>
              <div style={{ marginTop: 12, fontSize: 12 }}>
                {cFlags.length === 0 ? (
                  <span style={{ color: "#1D9E75" }}>Healthy</span>
                ) : (
                  cFlags.map((f) => (
                    <div key={f.id} style={{ color: SEVERITY_COLOR[f.severity], marginBottom: 2 }}>
                      ● {f.flagType.replace("_", " ")}
                    </div>
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>

      {selected && (
        <div
          onClick={() => setSelected(null)}
          style={{
            position: "fixed", inset: 0, background: "rgba(0,0,0,0.4)",
            display: "flex", alignItems: "center", justifyContent: "center",
          }}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{ background: "#fff", borderRadius: 12, padding: 24, maxWidth: 480 }}
          >
            <h3 style={{ fontSize: 16, fontWeight: 500, marginBottom: 12 }}>What the agent found</h3>
            {selected.map((f) => (
              <p key={f.id} style={{ fontSize: 14, lineHeight: 1.6, marginBottom: 12 }}>
                {f.explanation}
              </p>
            ))}
            <button onClick={() => setSelected(null)} style={{ fontSize: 13, color: "#5F5E5A" }}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
