$(() => {
    const ownedPetrolStationsSearchBtn = $("#ownedPetrolStationsSearchBtn");
    const ownedPetrolStationsTbody = $("#ownedPetrolStationsTbody");

    const addPetrolStationShowModal = $("#addPetrolStationShowModal");
    const addPetrolStationModal = $("#addPetrolStationModal").modal({});
    const addPetrolStationAccept = $("#addPetrolStationAccept");
    const addPetrolStationUniqueName = $("#addPetrolStationUniqueName");

    const editPetrolStationModal = $("#editPetrolStationModal").modal({});
    const editUniqueName = $("#editUniqueName");
    const editLogo = $("#editLogo");
    const editCommonName = $("#editCommonName");
    const editDescription = $("#editDescription");
    const saveDescriptionBtn = $("#saveDescriptionBtn");
    const editLongitude = $("#editLongitude")
    const editLatitude = $("#editLatitude")
    const editPetrolPb95 = $("#editPetrolPb95");
    const editPetrolPb98 = $("#editPetrolPb98");
    const editPetrolDiesel = $("#editPetrolDiesel");
    const savePetrolBtn = $("#savePetrolBtn");

    const searchNearPetrolStationsBtn = $("#searchNearPetrolStationsBtn");
    const nearPetrolStationsTbody = $("#nearPetrolStationsTbody");
    const nearPetrolStationsRadius = $("#nearPetrolStationsRadius");

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

            async function handleLineStatusUpdate() {
                const [isStation, isPylon] = await Promise.all([
                    myFetch("/api/petrolStation/isOnline", stationUniqueName),
                    myFetch("/api/pylon/isOnline", stationUniqueName)
                ]);

                isOnlineSpan.text([
                    isStation ? "Online" : "Offline",
                    isPylon ? "Online" : "Offline"
                ].join("/"));
            }

            const editButton = $("<button>", {
                text: "Edytuj",
                type: "button",
                class: "btn btn-primary",
                click: () => openEditPetrolStationModal(stationUniqueName)
            })

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: stationUniqueName}),
                    $("<td>", {append: [isOnlineSpan]}),
                    $("<td>", {append: [editButton]})
                ],
                appendTo: ownedPetrolStationsTbody
            });

            handleLineStatusUpdate();
        }
    }

    function openEditPetrolStationModal(stationName) {
        editPetrolStationModal.modal("show");

        editUniqueName.val(stationName);

        myFetch("/api/petrolStation/getStationDescription", stationName).then(stationDescription => {
            editLogo.val(stationDescription.logo);
            editCommonName.val(stationDescription.commonName);
            editDescription.val(stationDescription.description);
            editLatitude.val(stationDescription.latitude);
            editLongitude.val(stationDescription.longitude);
        });

        myFetch("/api/petrolStation/getCurrentPetrolPrice", stationName).then(petrolprice => {
            editPetrolPb95.val(petrolprice.pb95);
            editPetrolPb98.val(petrolprice.pb98);
            editPetrolDiesel.val(petrolprice.diesel);
        });
    }

    async function saveDescription() {
        myFetch("/api/petrolStation/setStationDescription", {
            uniqueName: editUniqueName.val(),
            stationDescription: {
                logo: editLogo.val(),
                commonName: editCommonName.val(),
                description: editDescription.val(),
                latitude: editLatitude.val(),
                longitude: editLongitude.val()
            }
        });
    }

    async function savePetrol() {
        myFetch("/api/petrolStation/setStationPrices", {
            uniqueName: editUniqueName.val(),
            petrolPrice:{
                pb95: editPetrolPb95.val(),
                pb98: editPetrolPb98.val(),
                diesel: editPetrolDiesel.val()
            }
        });
    }

    async function searchNearPetrolStations() {
        const stations = await myFetch("/api/this/findNearPetrolStation",{
            radius: nearPetrolStationsRadius.val()
        }) ?? [];

        nearPetrolStationsTbody.empty();

        let i = 0;
        for (const station of stations) {

            const isOnlineSpan = $("<span>");

            const editButton = $("<button>", {
                text: "Pokaż",
                type: "button",
                class: "btn btn-primary",
                click: () => {}
            });

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: station.stationDescription.commonName}),
                    $("<td>", {text: station.stationDescription.latitude+"/"+station.stationDescription.longitude}),
                    $("<td>", {append: JSON.stringify(station.petrolPrice)}),
                    $("<td>", {append: [editButton]})
                ],
                appendTo: nearPetrolStationsTbody
            });
        }
    }

    searchNearPetrolStationsBtn.click(searchNearPetrolStations);
    ownedPetrolStationsSearchBtn.click(searchOwnedPetrolStations);
    addPetrolStationShowModal.click(openAddPetrolStationModal);
    addPetrolStationAccept.click(createNewPetrolStation);
    savePetrolBtn.click(savePetrol);
    saveDescriptionBtn.click(saveDescription);
});