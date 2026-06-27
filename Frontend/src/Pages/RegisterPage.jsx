import { useState, useEffect } from "react"
import "./RegisterPage.css"

function RegisterPage() {

    const [showFeatures, setShowFeatures] = useState(true)
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [error, setError] = useState("")

    useEffect(() => {
        const handleEnter = (e) => {
            if (e.key == "Enter") {
                register(username, password)
            }
        }

        document.addEventListener("keydown", handleEnter)

        return () => {
            document.removeEventListener("keydown", handleEnter)
        }
    }, [username, password])

    async function register(username, password) {
        if (username === "" || password === "") {
            setError("All fields are mandatory")
            return
        }
        try {
            const response = await fetch("/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            })

            if (response.status === 201) {
                window.location.href = "/login";
                return
            }

            try {
                const body = await response.json()
                setError(body.error || "Registration failed")
            } catch {
                setError("Registration failed")
            }
        } catch (error) {
            setError("Could not reach the server")
        }
    }

    return (
        <>
            <div className="main-container">
                <div className="left-container">
                    <img src="/space-bg.png" className="left-background"></img>
                    <div className="img-text">
                        <h1 className="img-text-heading">Create your Account</h1>
                        <p className="img-text-subheading">Seamless feature rollout, effortless management.</p>
                        <div className="explore">
                            <div className="explore-button" onClick={() => setShowFeatures(!showFeatures)}>
                                <p className="explore-text">Explore</p>
                                <img src="/up-arrow.png" className={showFeatures ? "up-arrow-rotated" : "up-arrow"}></img>
                            </div>
                            <ul className={showFeatures ? "feature-list" : "feature-list-hidden"}>
                                <li>
                                    <img src="/check.png" className="check-image"></img>
                                    Easy flag creation & management
                                </li>
                                <li>
                                    <img src="/check.png" className="check-image"></img>
                                    Smart user targeting
                                </li>
                                <li>
                                    <img src="/check.png" className="check-image"></img>
                                    Secure login & access
                                </li>
                                <li>
                                    <img src="/check.png" className="check-image"></img>
                                    Gradual rollouts by percentage
                                </li>
                                <li>
                                    <img src="/check.png" className="check-image"></img>
                                    Flexible rules by attributes
                                </li>
                            </ul>
                        </div>
                    </div>
                </div>
                <div className="right-container">
                    <div className="form">
                        <p className="sign-up-text">Sign up for FlagShip</p>

                        <p className="form-label">Username</p>
                        <input type="text" id="username" placeholder="Enter Username" className="form-input" onChange={(e) => setUsername(e.target.value)}></input>

                        <p className="form-label">Password</p>
                        <input type="password" id="password" placeholder="Enter Password" className="form-input" onChange={(e) => setPassword(e.target.value)}></input>

                        <p className="form-label">Confirm Password</p>
                        <input type="password" id="password" placeholder="Confirm Password" className="form-input" onChange={(e) => {
                            if (e.target.value != password) {
                                setError("Passwords do not match!")
                            } else {
                                setError("")
                            }
                        }}></input>

                        <p className="error">{error}</p>

                        <button className="create-account-button" onClick={() => register(username, password)}>Create Account</button>

                        <div className="sign-in-prompt">
                            <p>Already a user?</p>
                            <a href="/login">Sign in ➙</a>
                        </div>
                    </div>

                </div>
            </div>
        </>
    )
}

export default RegisterPage;