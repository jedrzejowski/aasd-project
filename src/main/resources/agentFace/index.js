const agentName = document.getElementById("agent-name");
const agentClass = document.getElementById("agent-class");

async function myFetch(name, body) {
    const response = await fetch(`/${name}`, {
        method: body ? "POST" : "GET",
        body: body
    });

    return response.json()
}

async function setupUI() {
    const [name, clss] = await Promise.all([
        myFetch("name"),
        myFetch("class")
    ]);

    agentName.innerText = name;
    agentClass.innerText = clss;

    let className = clss.match(/[A-Za-z]+$/)[0];
    $("body > ." + className).show();

    $("<link/>", {
        rel: "stylesheet",
        href: className + ".css",
        appendTo: document.head
    });

    $("<script/>", {
        src: className + ".js",
        appendTo: document.body
    });
}

const stationsTbody = $("#stations-tbody");

async function refreshPetrolStations() {
    const stations = await myFetch("getPetrolStations");
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

    myFetch("getPetrolStation", station.name).then(data => {
        console.log(data);
    });

    return card;
}


$("#search-petrol-stations").click(refreshPetrolStations);
setupUI();