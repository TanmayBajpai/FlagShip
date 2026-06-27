import { useEffect, useState } from "react"
import "./RegisterPage.css"

function LoginPage() {

    const [showFeatures, setShowFeatures] = useState(true)
    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [error, setError] = useState("")

    useEffect(() => {
        const handleEnter = (e) => {
            if (e.key == "Enter") {
                login(username, password)
            }
        }

        document.addEventListener("keydown", handleEnter)
    
        return () => {
            document.removeEventListener("keydown", handleEnter)
        }
    }, [username, password])

    async function login(username, password) {
        if (username === "" || password === "") {
            setError("All fields are mandatory")
            return
        }
        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ username, password })
            })

            if (response.status === 200) {
                window.location.href = "/";
            } else {
                setError("Bad Credentials")
            }
        } catch (error) {
            console.log(error)
        }
    }

    return (
        <>
            <div className="main-container">
                <div className="left-container">
                    <img src="/space-bg.png" className="left-background"></img>
                    <div className="img-text">
                        <h1 className="img-text-heading">Welcome Back</h1>
                        <p className="img-text-subheading">Easily view and manage your Feature Flags.</p>
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
                        <p className="sign-up-text">Sign in</p>

                        <p className="form-label">Username</p>
                        <input type="text" id="username" placeholder="Enter Username" className="form-input" onChange={(e) => setUsername(e.target.value)}></input>

                        <p className="form-label">Password</p>
                        <input type="password" id="password" placeholder="Enter Password" className="form-input" onChange={(e) => setPassword(e.target.value)}></input>

                        <p className="error">{error}</p>

                        <button className="create-account-button" onClick={() => login(username, password)}>Sign in</button>

                        <div className="sign-in-prompt">
                            <p>Do not have an account?</p>
                            <a href="/register">Sign up ➙</a>
                        </div>
                    </div>

                </div>
            </div >
        </>
    )
}

export default LoginPage;
