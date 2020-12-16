const agentName = document.getElementById("agent-name");
const agentClass = document.getElementById("agent-class");

async function ping(name, body) {
    const response = await fetch(`/${name}`, {
        method: body ? "POST" : "GET",
        body: body
    });

    return response.json()
}

async function setupUI() {
    const [name, clss] = await Promise.all([
        ping("name"),
        ping("class")
    ]);

    agentName.innerText = name;
    agentClass.innerText = clss;

    document.body.classList.add(clss.match(/[A-Za-z]+$/)[0].toLowerCase())
}

async function refreshPetrolStations() {
    console.log("HERE")
}

document.getElementById("find-petrol-stations").onclick = refreshPetrolStations;

setupUI();