import "./Toast.css"

function Toast({ message, visible, type }) {
    return (
        <div className={`toast toast--${type}${visible ? " toast--visible" : ""}`}>
            {message}
        </div>
    )
}

export default Toast
