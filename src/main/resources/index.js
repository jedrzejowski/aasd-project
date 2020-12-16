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

const stationsTbody = $("#stations-tbody");

async function refreshPetrolStations() {
    const stations = await ping("getPetrolStations");
    stationsTbody.empty();
    let i = 0;

    for (const station of stations) {
        i++;
        const tr = $("<tr>")

        $("<td>", {
            text: i,
            appendTo: tr,
        });

        const content = $("<td>", {
            text: station.name,
            appendTo: tr,
        });

        $("<a>", {
            text: "Pokaż",
            class: "link-primary",
            href: "#",
            click: () => {
                createPetrolStationCard(station).appendTo(content.empty());
            }
        }).wrap("<td>").parent().appendTo(tr);

        stationsTbody.append(tr);
    }
}

function createPetrolStationCard(station) {
    const card = $(`<div class="card">`);

    const cardHeader = $(`<div/>`, {
        class: "card-header",
        text: station.name,
        appendTo: card,
    });

    ping("getPetrolStation", station.name).then(data => {
        console.log(data);
    });

    return card;
}


$("#search-petrol-stations").click(refreshPetrolStations);
setupUI();