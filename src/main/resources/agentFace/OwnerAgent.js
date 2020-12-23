$(() => {
    const ownedPetrolStationsSearchBtn = $("#ownedPetrolStationsSearchBtn");
    const ownedPetrolStationsTbody = $("#ownedPetrolStationsTbody");

    const addPetrolStationShowModal = $("#addPetrolStationShowModal");
    const addPetrolStationModal = $("#addPetrolStationModal").modal({});
    const addPetrolStationAccept = $("#addPetrolStationAccept");
    const addPetrolStationUniqueName = $("#addPetrolStationUniqueName");

    function openAddPetrolStationModal() {
        addPetrolStationUniqueName.val("");
        addPetrolStationModal.modal("show");
    }

    function closeAddPetrolStationModal() {
        addPetrolStationModal.modal("hide");
    }

    async function createNewPetrolStation() {
        const uniqueName = addPetrolStationUniqueName.val();

        try {
            await myFetch("/api/this/createPetrolStation", {
                uniqueName
            });

            makeAlert({text: "Utworzono agenta", type: "success"});
        } catch (e) {
            makeAlert({text: "Błąd tworzenia agenta", type: "error"});
        }

        closeAddPetrolStationModal();
    }

    async function searchOwnedPetrolStations() {
        ownedPetrolStationsTbody.empty();

        const owned = await myFetch("/api/this/ownedPetrolStation") ?? [];
        let i = 0;

        for (const stationUniqueName of owned) {

            const isOnlineSpan = $("<span>");

            function handleLineStatusUpdate() {
                myFetch("/api/petrolStation/isOnline", stationUniqueName).then(is => {
                    isOnlineSpan.text(is ? "Online" : "Offline");
                });
            }

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: stationUniqueName}),
                    $("<td>", {append: [isOnlineSpan]}),
                ],
                appendTo: ownedPetrolStationsTbody
            });

            handleLineStatusUpdate();
        }
    }

    ownedPetrolStationsSearchBtn.click(searchOwnedPetrolStations);
    addPetrolStationShowModal.click(openAddPetrolStationModal);
    addPetrolStationAccept.click(createNewPetrolStation);
});