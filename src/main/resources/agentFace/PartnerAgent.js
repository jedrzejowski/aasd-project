$(() => {
    const promotionsSearchBtn = $("#promotionsSearchBtn");
    const promotionsTbody = $("#promotionsTbody");

    const addPromotionShowModal = $("#addPromotionShowModal");
    const addPromotionModal = $("#addPromotionModal").modal({});
    const addPromotionAccept = $("#addPromotionAccept");
    const addPromotionName = $("#addPromotionName");
    const addPromotionDescription = $("#addPromotionDescription");

    // const editPetrolStationModal = $("#editPetrolStationModal").modal({});
    // const editUniqueName = $("#editUniqueName");
    // const editLogo = $("#editLogo");
    // const editCommonName = $("#editCommonName");
    // const editDescription = $("#editDescription");
    // const saveDescriptionBtn = $("#saveDescriptionBtn");
    // const editLongitude = $("#editLongitude")
    // const editLatitude = $("#editLatitude")
    // const editPetrolPb95 = $("#editPetrolPb95");
    // const editPetrolPb98 = $("#editPetrolPb98");
    // const editPetrolDiesel = $("#editPetrolDiesel");
    // const savePetrolBtn = $("#savePetrolBtn");

    function openAddPetrolStationModal() {
        addPromotionName.val("");
        addPromotionName.val("");
        addPromotionModal.modal("show");
    }

    function closeAddPetrolStationModal() {
        addPromotionModal.modal("hide");
    }

    async function createNewPromotion() {
        const name = addPromotionName.val();
        const description = addPromotionDescription.val();

        try {
            await myFetch("/api/this/createPromotion", {
                id: name,
                description: description

            });

            makeAlert({text: "Utworzono promocję", type: "success"});
        } catch (e) {
            makeAlert({text: "Błąd tworzenia promocji", type: "error"});
        }

        closeAddPetrolStationModal();
    }

    async function searchOwnedPetrolStations() {
        promotionsTbody.empty();

        const promotions = await myFetch("/api/this/getPromotions") ?? [];
        let i = 0;

        for (const promotion of promotions) {

            const isOnlineSpan = $("<span>");

            // async function handleLineStatusUpdate() {
            //     const [isStation, isPylon] = await Promise.all([
            //         myFetch("/api/petrolStation/isOnline", stationUniqueName),
            //         myFetch("/api/pylon/isOnline", stationUniqueName)
            //     ]);
            //
            //     isOnlineSpan.text([
            //         isStation ? "Online" : "Offline",
            //         isPylon ? "Online" : "Offline"
            //     ].join("/"));
            // }

            const editButton = $("<button>", {
                text: "Edytuj",
                type: "button",
                class: "btn btn-primary",
                //click: () => openEditPetrolStationModal(stationUniqueName)
            })

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: promotion.id}),
                    $("<td>", {append: promotion.description}),
                    $("<td>", {append: [editButton]})
                ],
                appendTo: promotionsTbody
            });

//            handleLineStatusUpdate();
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

    }

    promotionsSearchBtn.click(searchOwnedPetrolStations);
    addPromotionShowModal.click(openAddPetrolStationModal);
    addPromotionAccept.click(createNewPromotion);
    savePetrolBtn.click(savePetrol);
    saveDescriptionBtn.click(saveDescription);
});