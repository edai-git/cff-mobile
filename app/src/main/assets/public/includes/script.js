let timerId = null;
const label = document.getElementById('autoJbLabel');
const checkbox = document.getElementById('autoJbInput');
const delayInput = document.getElementById('autoJbDelayInput');
const jeilbrekBtn = document.getElementById('jeilbrek');
const UAElement = document.getElementById("UA");

// Auto Jailbreak Config
const autoJbDefault = false;
const storedAutoJb = localStorage.getItem("autoJb");
let autoJbValue = storedAutoJb !== null ? storedAutoJb === "true" : autoJbDefault;

const storedAutoJbDelay = localStorage.getItem("autoJbDelay");
let autoJbDelay = storedAutoJbDelay !== null ? parseInt(storedAutoJbDelay, 10) : 5;
if (isNaN(autoJbDelay) || autoJbDelay < 1) autoJbDelay = 5;

// choose one of kernel exploits
var exploitChain = localStorage.getItem("exploitChain") || "lapse";
const netctrlRadio = document.getElementById("netctrl-exploit");
const lapseRadio = document.getElementById("lapse-exploit");
const kexForm = document.getElementById('kernel-options');

// Show user agent
UAElement.innerText += " " + navigator.userAgent;

kexForm.addEventListener("change", function (event) {
    localStorage.setItem("exploitChain", event.target.value);
    exploitChain = event.target.value;
});

const reloadBtn = document.getElementById('reloadBtn');

// jailbreak execution
jeilbrekBtn.addEventListener("click", function (e) {
    jeilbrekBtn.disabled = true;
    stopInterval();
    doJb();
});

if (reloadBtn) {
    reloadBtn.addEventListener("click", function () {
        stopInterval();
        window.location.reload();
    });
}

checkbox.addEventListener('change', function () {
    localStorage.setItem("autoJb", checkbox.checked);
    if (checkbox.checked == true && jeilbrekBtn.disabled == false) {
        jailbreakCountdown();
        return;
    }

    stopInterval();
});

if (delayInput) {
    delayInput.value = autoJbDelay;
    delayInput.addEventListener("change", function () {
        let val = parseInt(delayInput.value, 10);
        if (isNaN(val) || val < 1) val = 1;
        if (val > 99) val = 99;
        delayInput.value = val;
        autoJbDelay = val;
        localStorage.setItem("autoJbDelay", val);
        if (checkbox.checked && jeilbrekBtn.disabled === false) {
            jailbreakCountdown();
        }
    });
}

function stopInterval() {
    if (timerId !== null) {
        clearInterval(timerId);
        timerId = null;
    }
    label.textContent = "Auto Jailbreak";
}

function jailbreakCountdown() {
    stopInterval();

    let countdown = autoJbDelay;
    label.textContent = `Auto Jailbreaking in: ${countdown}s`;
    timerId = setInterval(() => {
        countdown--;
        label.textContent = `Auto Jailbreaking in: ${countdown}s`;

        if (countdown < 0) {
            jeilbrekBtn.disabled = true;
            clearInterval(timerId);
            timerId = null;
            label.textContent = 'Executing';
            doJb();
        }
    }, 1000);
}

function cacheProgress(e) {
    var Percent = (Math.round(e.loaded / e.total * 100));
    document.title = "Caching: " + Percent + "%";
}

function displayCacheProgress() {
    setTimeout(function () {
        // show a tick
        document.title = "\u2713";
    }, 1000);
    setTimeout(function () {
        // location.reload();
        document.title = "CSSFontFace exploit";
    }, 3000);
}

document.addEventListener("DOMContentLoaded", function () {
    // Cache handling
    if (window.applicationCache) {
        window.applicationCache.addEventListener("progress", cacheProgress, false);
        window.applicationCache.oncached = function (e) { displayCacheProgress(); };
        window.applicationCache.onupdateready = function (e) { displayCacheProgress(); };
    }

    // choose prefered exploit chain
    if (exploitChain == "netctrl") {
        netctrlRadio.checked = true;
    } else {
        lapseRadio.checked = true;
    }

    // apply autojb localStorage value
    checkbox.checked = autoJbValue;
    if (delayInput) delayInput.value = autoJbDelay;

    if (autoJbValue) jailbreakCountdown();
});