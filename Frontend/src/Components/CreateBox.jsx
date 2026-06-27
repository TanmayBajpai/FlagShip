import { useState } from "react"
import "../Components/CreateBox.css"

function CreateBox({ setOverlay, setBox, setChanged, showToast }) {

    const [rules, setRules] = useState([])
    const [newValueInputs, setNewValueInputs] = useState([])
    const [info, setInfo] = useState("")

    function addRule() {
        setRules(prev => [...prev, { key: "", values: [], enabled: true }])
        setNewValueInputs(prev => [...prev, ""])
    }

    function removeRule(index) {
        setRules(prev => prev.filter((_, i) => i !== index))
        setNewValueInputs(prev => prev.filter((_, i) => i !== index))
    }

    function updateRuleKey(index, key) {
        setRules(prev => prev.map((r, i) => i === index ? { ...r, key } : r))
    }

    function toggleRule(index) {
        setRules(prev => prev.map((r, i) => i === index ? { ...r, enabled: !r.enabled } : r))
    }

    function addValue(index) {
        const value = newValueInputs[index].trim()
        if (!value || rules[index].values.includes(value)) return
        setRules(prev => prev.map((r, i) => i === index ? { ...r, values: [...r.values, value] } : r))
        setNewValueInputs(prev => prev.map((v, i) => i === index ? "" : v))
    }

    function removeValue(ruleIndex, value) {
        setRules(prev => prev.map((r, i) => i === ruleIndex ? { ...r, values: r.values.filter(v => v !== value) } : r))
    }

    async function createFlag() {
        const flagName = document.getElementById("flagName").value
        const description = document.getElementById("description").value
        const rolloutPercent = document.getElementById("rolloutPercent").value

        if (!flagName) { setInfo("Flag Name cannot be empty!"); return }
        if (!rolloutPercent || isNaN(rolloutPercent) || rolloutPercent < 0 || rolloutPercent > 100) {
            setInfo("Rollout Percent must be between 0–100"); return
        }

        const targetingRules = rules
            .filter(({ key, values }) => key && values.length > 0)
            .map(({ key, values, enabled }) => ({ key, values, enabled }))

        const payload = {
            flagName,
            description: description || null,
            enabled: rolloutPercent > 0,
            rolloutPercent: Number(rolloutPercent),
            targetingRules
        }

        const response = await fetch("/flags", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        })

        if (response.status === 409) { setInfo("A flag with this name already exists"); return }
        setBox(false); setOverlay(false); setChanged(p => !p)
        showToast("Flag created!")
    }

    return (
        <div className="modal-box">
            <div className="modal-title">Create a new Feature Flag</div>
            <form className="modal-form">
                <label className="field-label">Flag Name</label>
                <input type="text" className="field-input" id="flagName" placeholder="my-feature" onChange={() => setInfo("")} />

                <label className="field-label">Description</label>
                <input type="text" className="field-input" id="description" placeholder="What does this flag do?" />

                <label className="field-label">Rollout Percent</label>
                <input type="number" className="field-input" id="rolloutPercent" min="0" max="100" placeholder="0–100" onChange={() => setInfo("")} />

                <label className="field-label">Targeting Rules</label>
                <p className="rules-hint">Add attribute rules to restrict who sees this flag. Leave empty for all users.</p>

                <div className="rules-container">
                    {rules.map((rule, index) => (
                        <div key={index} className={`rule-block${rule.enabled ? "" : " rule-block--off"}`}>
                            <div className="rule-block-header">
                                <div className="rule-block-header-left">
                                    <label className="toggle-switch">
                                        <input type="checkbox" checked={rule.enabled} onChange={() => toggleRule(index)} />
                                        <span className="toggle-track"><span className="toggle-thumb" /></span>
                                    </label>
                                    <span className="rule-section-label">Attribute</span>
                                </div>
                                <button type="button" className="btn-remove-rule" onClick={() => removeRule(index)}>Remove</button>
                            </div>
                            <input
                                type="text"
                                className="rule-key-input"
                                placeholder="e.g. country, plan, deviceType"
                                value={rule.key}
                                onChange={(e) => updateRuleKey(index, e.target.value)}
                            />
                            <span className="rule-section-label">Allowed Values</span>
                            <div className="add-value-row">
                                <input
                                    type="text"
                                    className="value-input"
                                    placeholder="Type a value and press Enter or +"
                                    value={newValueInputs[index] || ""}
                                    onChange={(e) => setNewValueInputs(prev => prev.map((v, i) => i === index ? e.target.value : v))}
                                    onKeyDown={(e) => { if (e.key === "Enter") { e.preventDefault(); addValue(index) } }}
                                />
                                <button type="button" className="btn-add-value" onClick={() => addValue(index)}>+</button>
                            </div>
                            {rule.values.length > 0 && (
                                <div className="value-chips">
                                    {rule.values.map(value => (
                                        <div key={value} className="value-chip">
                                            <span>{value}</span>
                                            <button type="button" className="btn-remove-chip" onClick={() => removeValue(index, value)}>×</button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    ))}
                    <button type="button" className="btn-add-rule" onClick={addRule}>+ Add Rule</button>
                </div>

                <p className="form-error">{info}</p>

                <div className="modal-footer">
                    <button type="button" className="btn-cancel" onClick={() => { setRules([]); setNewValueInputs([]); setOverlay(false); setBox(false) }}>Cancel</button>
                    <button type="button" className="btn-submit" onClick={createFlag}>Create Flag</button>
                </div>
            </form>
        </div>
    )
}

export default CreateBox
