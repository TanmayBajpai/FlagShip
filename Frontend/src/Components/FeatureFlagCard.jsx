import { useState } from "react"
import "./FeatureFlagCard.css"

function FeatureFlagCard({ flag, setChanged, setUpdateBox, setOverlay, setFlag, setShowFlagId, showToast }) {

    const [toggling, setToggling] = useState(false)

    const rules = flag.targetingRules || []
    const activeRules = rules.filter(r => r.enabled).length

    const rollout = flag.rolloutPercent
    const flagConv = flag.flagConversions || 0
    const controlConv = flag.controlConversions || 0

    const flagNorm = rollout > 0 ? flagConv / rollout : null
    const controlNorm = rollout < 100 ? controlConv / (100 - rollout) : null
    const lift = (flagNorm !== null && controlNorm !== null && controlNorm > 0)
        ? (flagNorm - controlNorm) / controlNorm * 100
        : null
    const maxConv = Math.max(flagConv, controlConv, 1)

    function fmtNum(n) { return (n || 0).toLocaleString() }

    async function deleteFlag(id) {
        try {
            const response = await fetch(`/flags/${id}`, { method: "DELETE", credentials: "include" })
            if (!response.ok) { showToast("Invalid request", "error"); return }
            showToast("Flag deleted")
            setChanged(p => !p)
        } catch {
            showToast("Server error!", "error")
        }
    }

    async function resetFlag(id) {
        try {
            const response = await fetch(`/flags/${id}/reset`, { method: "POST", credentials: "include" })
            if (!response.ok) { showToast("Invalid request", "error"); return }
            showToast("Flag users reset")
            setChanged(p => !p)
        } catch {
            showToast("Server error!", "error")
        }
    }

    async function toggleRule(key) {
        if (toggling) return
        setToggling(true)
        const updatedRules = rules.map(r => ({
            key: r.key,
            values: r.values,
            enabled: r.key === key ? !r.enabled : r.enabled
        }))
        try {
            await fetch(`/flags/${flag.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ targetingRules: updatedRules })
            })
            setChanged(p => !p)
        } catch {
            showToast("Server error!", "error")
        } finally {
            setToggling(false)
        }
    }

    return (
        <div className="flag-container">

            <div className="flag-header">
                <div className="flag-name">{flag.flagName}</div>
                <div className="flag-header-actions">
                    <button className="icon-btn edit-btn" title="Edit" onClick={() => { setUpdateBox(true); setOverlay(true); setFlag(flag) }}>
                        <img src="/edit.png" className="icon-img" alt="edit" />
                    </button>
                    <button className="icon-btn delete-btn" title="Delete" onClick={() => deleteFlag(flag.id)}>
                        <img src="/delete.png" className="icon-img" alt="delete" />
                    </button>
                </div>
            </div>

            {flag.description && (
                <div className="flag-description">{flag.description}</div>
            )}

            <div className="flag-topbar">
                <div className="topbar-item">
                    <span className="topbar-label">Rollout</span>
                    <span className="topbar-value">{flag.rolloutPercent}%</span>
                </div>
                <div className="topbar-divider" />
                <div className="topbar-item">
                    <span className="topbar-label">Conversions</span>
                    <span className="topbar-value">{fmtNum(flagConv + controlConv)}</span>
                </div>
                <div className="topbar-divider" />
                <div className="topbar-item">
                    <span className="topbar-label">Lift</span>
                    <span className={`topbar-value ${lift === null ? "stat-muted" : lift >= 0 ? "stat-green" : "stat-red"}`}>
                        {lift === null ? "—" : (lift >= 0 ? "+" : "") + lift.toFixed(1) + "%"}
                    </span>
                </div>
            </div>

            <div className="flag-metrics">
                <div className="metrics-header">
                    <span className="section-label">Conversion Metrics</span>
                    <span className="metrics-legend">conversions</span>
                </div>
                <div className="metric-row">
                    <span className="metric-group-label flag-label">Flag</span>
                    <span className="metric-counts">{fmtNum(flagConv)}</span>
                    <div className="metric-bar-track">
                        <div className="metric-bar-fill bar-flag" style={{ width: `${(flagConv / maxConv) * 100}%` }} />
                    </div>
                </div>
                <div className="metric-row">
                    <span className="metric-group-label control-label">Control</span>
                    <span className="metric-counts">{fmtNum(controlConv)}</span>
                    <div className="metric-bar-track">
                        <div className="metric-bar-fill bar-control" style={{ width: `${(controlConv / maxConv) * 100}%` }} />
                    </div>
                </div>
                {flagConv === 0 && controlConv === 0 && (
                    <p className="metrics-empty">No data yet — call /api/success to start tracking</p>
                )}
            </div>

            <div className="flag-rules-section">
                <div className="flag-rules-header">
                    <span className="section-label">Targeting Rules</span>
                    <span className="rules-summary">
                        {rules.length === 0
                            ? "all users"
                            : `${activeRules} / ${rules.length} active`}
                    </span>
                </div>

                {rules.length === 0 ? (
                    <p className="rules-empty">No rules — all users qualify</p>
                ) : (
                    <div className="rules-list">
                        {rules.map((rule) => (
                            <div key={rule.key} className={`rule-row${rule.enabled ? "" : " rule-row--off"}`}>
                                <label className="toggle-switch" title={rule.enabled ? "Disable rule" : "Enable rule"}>
                                    <input
                                        type="checkbox"
                                        checked={rule.enabled}
                                        disabled={toggling}
                                        onChange={() => toggleRule(rule.key)}
                                    />
                                    <span className="toggle-track">
                                        <span className="toggle-thumb" />
                                    </span>
                                </label>
                                <span className="rule-key">{rule.key}</span>
                                <div className="rule-chips">
                                    {(rule.values || []).map(v => (
                                        <span key={v} className="rule-chip">{v}</span>
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="flag-footer">
                <button className="btn-ghost" onClick={() => resetFlag(flag.id)}>Reset Users</button>
                <button className="btn-ghost" onClick={() => { setFlag(flag); setOverlay(true); setShowFlagId(true) }}>
                    Show ID
                </button>
            </div>

        </div>
    )
}

export default FeatureFlagCard
