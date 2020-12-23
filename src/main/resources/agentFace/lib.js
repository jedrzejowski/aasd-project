const agentName = $("#agent-name");
const agentClass = $("#agent-class");

async function myFetch(name, body = undefined) {
    const response = await fetch(name, {
        method: body ? "POST" : "GET",
        body: body ? JSON.stringify(body) : undefined
    });

    return response.json()
}

function makeAlert({text, type = "primary"}) {
    $("<div/>", {
        class: `alert alert-${type}`,
        text,
        role: "alert",
        append: [
            $("<button>", {
                type: "button",
                class: "btn-close",
                "data-bs-dismiss": "alert",
            })
        ],
        appendTo: "body"
    }).alert();
}

$(async () => {
    const [name, clss] = await Promise.all([
        myFetch("name"),
        myFetch("class")
    ]);

    agentName.text(name);
    agentClass.text(clss);
});
