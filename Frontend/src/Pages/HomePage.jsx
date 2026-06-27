import { useEffect, useRef, useState } from "react"
import FeatureFlagCard from "../Components/FeatureFlagCard"
import UpdateBox from "../Components/UpdateBox"
import CreateBox from "../Components/CreateBox"
import Toast from "../Components/Toast"
import "./HomePage.css"

function HomePage() {

    const [apiKey, setApiKey] = useState("API Key Unavailable")
    const [flags, setFlags] = useState([])
    const [changed, setChanged] = useState(false)
    const [showApiKey, setShowApiKey] = useState(false)
    const [overlay, setOverlay] = useState(false)
    const [updateBox, setUpdateBox] = useState(false)
    const [createBox, setCreateBox] = useState(false)
    const [flag, setFlag] = useState()
    const [showFlagId, setShowFlagId] = useState()
    const [toast, setToast] = useState({ message: "", type: "success", visible: false })
    const toastTimer = useRef(null)

    function showToast(message, type = "success") {
        clearTimeout(toastTimer.current)
        setToast({ message, type, visible: true })
        toastTimer.current = setTimeout(() => setToast(prev => ({ ...prev, visible: false })), 2500)
    }

    useEffect(() => {
        let mounted = true;
        async function fetchUserInfo() {
            try {
                const response = await fetch("/auth/me", {
                    method: "GET",
                    credentials: "include"
                })

                if (!response.ok) {
                    window.location.href = "/login"
                    return
                }

                const userInfo = await response.json()

                if (mounted && userInfo) {
                    setApiKey(userInfo.apiKey)
                }
            } catch (error) {
                console.log(error)
                window.location.href = "/login";
            }
        }

        fetchUserInfo()

        return () => {
            mounted = false
        }
    }, [])

    useEffect(() => {
        let mounted = true

        async function fetchFlags() {
            const response = await fetch("/flags", {
                method: "GET",
                credentials: "include",
                cache: "no-cache"
            })

            setFlags(await response.json())
        }

        fetchFlags()

        return () => {
            mounted = false;
        }
    }, [changed])


    async function logout() {
        await fetch("/logout", {
            method: "POST",
            credentials: "include"
        })
        window.location.href = "/login"
    }

    return (
        <>
            {overlay && (
                <div
                    className="backdrop-overlay"
                    onClick={() => {
                        setOverlay(false);
                        setShowApiKey(false);
                        setUpdateBox(false);
                        setCreateBox(false);
                    }}
                />
            )}


            <div className="page">
                <div className="navbar">

                    <img src="/flag.png" className="flag-image"></img>
                    <p className="home-text">Dashboard</p>

                    <div className="navbar-buttons">
                        <button className="api-button" onClick={() => {
                            setShowApiKey(true)
                            setOverlay(true)
                        }}>
                            API Key
                            <img src="/code.png" className="api-image"></img>
                        </button>
                        <button className="logout-button" onClick={() => logout()}>
                            Log Out
                            <img src="/logout.png" className="logout-image"></img>
                        </button>
                    </div>

                </div>

                <div className="feature-flags">

                    <p className="feature-flags-text">Feature Flags</p>
                    <button className="create-flag-button" onClick={() => {
                        setCreateBox(true)
                        setOverlay(true)
                    }}>
                        + Create new Feature Flag
                    </button>

                    {flags.length === 0 && (
                        <div className="no-flags">{"< No flags to display... >"}</div>
                    )}

                    <div className="feature-flags-container">
                        {flags.map((flag) => (<FeatureFlagCard key={flag.id} flag={flag} setChanged={setChanged} setUpdateBox={setUpdateBox} setOverlay={setOverlay} setFlag={setFlag} setShowFlagId={setShowFlagId} showToast={showToast}/>))}
                    </div>
                </div>
            </div>

            {showApiKey && (
                <div className="api-box">

                    <div className="api-box-header">
                        <p>API Key</p>
                        <button className="close-api-button" onClick={() => {
                            setOverlay(false)
                            setShowApiKey(false)
                        }}>X</button>
                    </div>

                    <div className="api-key">
                        <p>{apiKey}</p>
                        <button className="copy-button" onClick={() => {
                            navigator.clipboard.writeText(apiKey)
                            showToast("API Key copied")
                        }}>
                            <img src="/copy.png"></img>
                        </button>
                    </div>

                </div>
            )}


            {showFlagId && (
                <div className="api-box">

                    <div className="api-box-header">
                        <p>Feature Flag ID</p>
                        <button className="close-api-button" onClick={() => {
                            setOverlay(false)
                            setShowFlagId(false)
                        }}>X</button>
                    </div>

                    <div className="api-key">
                        <p>{flag.id}</p>
                        <button className="copy-button" onClick={() => {
                            navigator.clipboard.writeText(flag.id)
                            showToast("Flag ID copied")
                        }}>
                            <img src="/copy.png"></img>
                        </button>
                    </div>

                </div>
            )}


            {createBox && (<CreateBox setOverlay={setOverlay} setBox={setCreateBox} setChanged={setChanged} showToast={showToast}/>)}
            {updateBox && (<UpdateBox setOverlay={setOverlay} setBox={setUpdateBox} setChanged={setChanged} flag={flag} showToast={showToast}/>)}

            <Toast message={toast.message} type={toast.type} visible={toast.visible} />

        </>
    )
}

export default HomePage