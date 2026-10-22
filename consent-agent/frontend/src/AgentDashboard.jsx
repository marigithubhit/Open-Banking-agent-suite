import React, { useState, useEffect } from "react";

const API = "http://localhost:8080/api";
const SEVERITY_COLOR = { HIGH: "#D85A30", MEDIUM: "#EF9F27", LOW: "#1D9E75" };

export default function AgentDashboard() {
  const [tab, setTab] = useState("consent");

  return (
    <div style={{ maxWidth: 960, margin: "0 auto", padding: 24, fontFamily: "sans-serif", color: "#2C2C2A" }}>
      <header style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 22, fontWeight: 500, marginBottom: 4 }}>Open Banking Agent Suite</h1>
        <p style={{ fontSize: 13, color: "#5F5E5A" }}>
          Orchestrator agent routing between Consent and Payment specialists. Runs on a local model — no data leaves this environment.
        </p>
      </header>

      <div style={{ display: "flex", gap: 8, marginBottom: 24, borderBottom: "1px solid #D3D1C7" }}>
        {["consent", "payment"].map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: "10px 16px", border: "none", background: "none", cursor: "pointer",
              fontSize: 14, fontWeight: tab === t ? 500 : 400,
              borderBottom: tab === t ? "2px solid #534AB7" : "2px solid transparent",
              color: tab === t ? "#534AB7" : "#5F5E5A",
            }}
          >
            {t === "consent" ? "Consent agent" : "Payment agent"}
          </button>
        ))}
      </div>

      {tab === "consent" ? <ConsentPanel /> : <PaymentPanel />}
    </div>
  );
}

function ConsentPanel() {
  const [consents, setConsents] = useState([]);
  const [flags, setFlags] = useState([]);
  const [scanning, setScanning] = useState(false);
  const [selected, setSelected] = useState(null);

  useEffect(() => { fetch(`${API}/consents`).then((r) => r.json()).then(setConsents); }, []);

  const runScan = async () => {
    setScanning(true);
    const res = await fetch(`${API}/consents/scan`, { method: "POST" });
    setFlags(await res.json());
    setScanning(false);
  };

  const flagsFor = (id) => flags.filter((f) => f.consentId === id);

  return (
    <div>
      <button onClick={runScan} disabled={scanning} style={btnStyle}>
        {scanning ? "Scanning..." : "Run agent scan"}
      </button>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(260px, 1fr))", gap: 16, marginTop: 20 }}>
        {consents.map((c) => {
          const cFlags = flagsFor(c.consentId);
          const worst = cFlags.reduce((acc, f) => {
            const order = { HIGH: 3, MEDIUM: 2, LOW: 1 };
            return order[f.severity] > (order[acc] || 0) ? f.severity : acc;
          }, null);
          return (
            <div key={c.consentId} onClick={() => cFlags.length && setSelected(cFlags)}
                 style={{ ...cardStyle, borderColor: worst ? SEVERITY_COLOR[worst] : "#B4B2A9", cursor: cFlags.length ? "pointer" : "default" }}>
              <div style={{ fontWeight: 500, fontSize: 15 }}>{c.tppName}</div>
              <div style={{ fontSize: 12, color: "#5F5E5A", marginTop: 4 }}>{c.scope}</div>
              <div style={{ marginTop: 12, fontSize: 12 }}>
                {cFlags.length === 0 ? <span style={{ color: "#1D9E75" }}>Healthy</span> :
                  cFlags.map((f) => <div key={f.id} style={{ color: SEVERITY_COLOR[f.severity] }}>● {f.flagType.replace("_", " ")}</div>)}
              </div>
            </div>
          );
        })}
      </div>

      {selected && (
        <Modal onClose={() => setSelected(null)}>
          <h3 style={{ fontSize: 16, fontWeight: 500, marginBottom: 12 }}>What the agent found</h3>
          {selected.map((f) => <p key={f.id} style={{ fontSize: 14, lineHeight: 1.6, marginBottom: 12 }}>{f.explanation}</p>)}
        </Modal>
      )}
    </div>
  );
}

function PaymentPanel() {
  const [command, setCommand] = useState("Pay 500 to John's account");
  const [routedTo, setRoutedTo] = useState(null);
  const [pending, setPending] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  const loadHistory = () => fetch(`${API}/payments/history`).then((r) => r.json()).then(setHistory);
  useEffect(() => { loadHistory(); }, []);

  const submitCommand = async () => {
    setLoading(true);
    const routeRes = await fetch(`${API}/agent/classify`, {
      method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ command }),
    });
    const { routedTo } = await routeRes.json();
    setRoutedTo(routedTo);

    const res = await fetch(`${API}/payments/initiate`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ command, accountId: "ACC-002" }),
    });
    setPending(await res.json());
    setLoading(false);
  };

  const respond = async (action) => {
    await fetch(`${API}/payments/${pending.id}/${action}`, { method: "POST" });
    setPending(null);
    loadHistory();
  };

  return (
    <div>
      <div style={{ display: "flex", gap: 8 }}>
        <input value={command} onChange={(e) => setCommand(e.target.value)}
               placeholder='e.g. "Pay 500 to John\'s account"'
               style={{ flex: 1, padding: "10px 14px", borderRadius: 8, border: "1px solid #D3D1C7", fontSize: 14 }} />
        <button onClick={submitCommand} disabled={loading} style={btnStyle}>
          {loading ? "Thinking..." : "Send to agent"}
        </button>
      </div>

      {routedTo && !pending?.status && (
        <p style={{ fontSize: 12, color: "#5F5E5A", marginTop: 8 }}>Orchestrator routed this to: <b>{routedTo}</b></p>
      )}

      {pending && pending.status === "PENDING_CONFIRMATION" && (
        <div style={{ ...cardStyle, borderColor: "#EF9F27", marginTop: 20, maxWidth: 420 }}>
          <div style={{ fontSize: 12, color: "#5F5E5A", marginBottom: 8 }}>Agent understood this as:</div>
          <div style={{ fontSize: 16, fontWeight: 500 }}>Pay {pending.amount} to {pending.payeeName}</div>
          <div style={{ fontSize: 12, color: "#5F5E5A", marginTop: 6 }}>{pending.validationNote}</div>
          <div style={{ display: "flex", gap: 8, marginTop: 16 }}>
            <button onClick={() => respond("confirm")} style={btnStyle}>Confirm & pay</button>
            <button onClick={() => respond("decline")} style={{ ...btnStyle, background: "#fff", color: "#5F5E5A", border: "1px solid #D3D1C7" }}>Decline</button>
          </div>
        </div>
      )}

      {pending && pending.status === "FAILED_VALIDATION" && (
        <div style={{ ...cardStyle, borderColor: "#D85A30", marginTop: 20, maxWidth: 420 }}>
          <div style={{ fontWeight: 500, color: "#D85A30" }}>Agent declined to propose this payment</div>
          <div style={{ fontSize: 13, marginTop: 6 }}>{pending.validationNote}</div>
        </div>
      )}

      <h3 style={{ fontSize: 15, fontWeight: 500, marginTop: 32, marginBottom: 12 }}>Transaction history</h3>
      <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
        {history.map((h) => (
          <div key={h.id} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, padding: "8px 12px", border: "1px solid #D3D1C7", borderRadius: 8 }}>
            <span>{h.payeeName} — {h.amount} {h.currency}</span>
            <span style={{ color: h.status === "EXECUTED" ? "#1D9E75" : h.status === "DECLINED" ? "#5F5E5A" : "#D85A30" }}>
              {h.status}{h.transactionRef ? ` (${h.transactionRef})` : ""}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function Modal({ children, onClose }) {
  return (
    <div onClick={onClose} style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.4)", display: "flex", alignItems: "center", justifyContent: "center" }}>
      <div onClick={(e) => e.stopPropagation()} style={{ background: "#fff", borderRadius: 12, padding: 24, maxWidth: 480 }}>
        {children}
        <button onClick={onClose} style={{ fontSize: 13, color: "#5F5E5A", marginTop: 8 }}>Close</button>
      </div>
    </div>
  );
}

const cardStyle = { border: "1px solid #B4B2A9", borderRadius: 12, padding: 16, background: "#fff" };
const btnStyle = { padding: "10px 18px", borderRadius: 8, border: "none", background: "#534AB7", color: "white", cursor: "pointer", fontSize: 14 };
