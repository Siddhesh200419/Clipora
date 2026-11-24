const content = document.getElementById('content');
const shareStatus = document.getElementById('share-status');
const receiveStatus = document.getElementById('receive-status');

function showStatus(element, message, isError = false) {
    element.textContent = message;
    element.classList.toggle('error', isError);
}

function expiryMessage(date) {
    return 'Expires at ' + new Date(date).toLocaleTimeString();
}

content.addEventListener('input', function () {
    document.getElementById('character-count').textContent = content.value.length.toLocaleString() + ' characters';
});

document.getElementById('share-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const button = document.getElementById('share-button');
    document.getElementById('share-result').hidden = true;
    if (!content.value.trim()) {
        showStatus(shareStatus, 'Please enter some text.', true);
        return;
    }
    button.disabled = true;
    showStatus(shareStatus, 'Saving your text…');
    try {
        // A relative URL works both at / and at /clipora/ on Tomcat.
        const response = await fetch('api/clipboards', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain;charset=UTF-8' },
            body: content.value
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Could not save text.');
        document.getElementById('share-code').textContent = data.code;
        document.getElementById('share-expiry').textContent = expiryMessage(data.expiresAt);
        document.getElementById('share-result').hidden = false;
        showStatus(shareStatus, 'Ready! Enter this code on your other device.');
    } catch (error) {
        showStatus(shareStatus, error instanceof TypeError || error instanceof SyntaxError
            ? 'Unable to reach the app. Please try again.' : error.message, true);
    } finally {
        button.disabled = false;
    }
});

document.getElementById('receive-form').addEventListener('submit', async function (event) {
    event.preventDefault();
    const button = document.getElementById('receive-button');
    const code = document.getElementById('code').value.trim();
    document.getElementById('receive-result').hidden = true;
    document.getElementById('received-text').value = '';
    button.disabled = true;
    showStatus(receiveStatus, 'Looking up your code…');
    try {
        const response = await fetch('api/clipboards?code=' + encodeURIComponent(code));
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || 'Could not retrieve text.');
        // Use value, not innerHTML, to display shared text safely.
        document.getElementById('received-text').value = data.content;
        document.getElementById('receive-expiry').textContent = expiryMessage(data.expiresAt);
        document.getElementById('receive-result').hidden = false;
        showStatus(receiveStatus, 'Your text is ready.');
    } catch (error) {
        showStatus(receiveStatus, error instanceof TypeError || error instanceof SyntaxError
            ? 'Unable to reach the app. Please try again.' : error.message, true);
    } finally {
        button.disabled = false;
    }
});

async function copyText(text, status) {
    try {
        await navigator.clipboard.writeText(text);
        showStatus(status, 'Copied!');
    } catch (error) {
        showStatus(status, 'Copy is unavailable. Select the text and copy it manually.', true);
    }
}

document.getElementById('copy-code').addEventListener('click', function () {
    copyText(document.getElementById('share-code').textContent, shareStatus);
});
document.getElementById('copy-text').addEventListener('click', function () {
    copyText(document.getElementById('received-text').value, receiveStatus);
});
