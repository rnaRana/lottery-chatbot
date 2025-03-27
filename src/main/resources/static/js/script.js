function toggleChat() {
    let chatContainer = document.querySelector(".chatbot-container");
    let reopenButton = document.getElementById("reopen-chat");

    let isHidden = chatContainer.style.display === "none" || chatContainer.style.display === "";
    chatContainer.style.display = isHidden ? "block" : "none";
    reopenButton.classList.toggle("hidden", isHidden);
}




async function sendMessage() {
    let userInput = document.getElementById("user-input");
    let message = userInput.value.trim();
    
    if (message === "") return;

    let chatBox = document.getElementById("chat-box");

    // Append User Message
    chatBox.innerHTML += `<div class="message user-message"><strong>You:</strong> ${message}</div>`;

    userInput.value = "";

    // Send request to chatbot backend
    try {
        const response = await fetch("/api/chatbot/message", {  // ✅ Ensure correct API path
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: message })
        });

        const botResponse = await response.text();

        // Append Bot Response
        chatBox.innerHTML += `<div class="message bot-message"><strong>Bot:</strong> ${botResponse}</div>`;
        
    } catch (error) {
        chatBox.innerHTML += `<div class="message error-message"><strong>Bot:</strong> Error: Unable to contact chatbot.</div>`;
    }
	chatBox.scrollTop = chatBox.scrollHeight;

}
