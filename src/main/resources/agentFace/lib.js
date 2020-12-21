const agentName = $("#agent-name");
const agentClass = $("#agent-class");

async function myFetch(name, body = undefined) {
    const response = await fetch(name, {
        method: body ? "POST" : "GET",
        body: body ? JSON.stringify(body) : undefined
    });

    return response.json()
}

$(async () => {
    const [name, clss] = await Promise.all([
        myFetch("name"),
        myFetch("class")
    ]);

    agentName.text(name);
    agentClass.text(clss);
});
