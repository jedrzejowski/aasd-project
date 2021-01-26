$(() => {
    const editVehicleFuelLeft = $("#editVehicleFuelLeft");
    const editVehicleFuelPerKilometer = $("#editVehicleFuelPerKilometer");
    const editVehicleLatitude = $("#editVehicleLatitude");
    const editVehicleLongitude = $("#editVehicleLongitude");
    const saveVehicleData = $("#saveVehicleData");
    const searchCheapestPetrolStationsBtn = $("#searchCheapestPetrolStationsBtn");
    const searchNearPetrolStationsBtn = $("#searchNearPetrolStationsBtn");
    const nearPetrolStationsTable = $("#nearPetrolStationsTable");
    const radius = $("#radius");

    const votePetrolStationModal = $("#votePetrolStationModal").modal({});
    const votePetrolUniqueName = $("#votePetrolUniqueName");
    const votePetrolPb95 = $("#votePetrolPb95");
    const votePetrolPb98 = $("#votePetrolPb98");
    const votePetrolDiesel = $("#votePetrolDiesel");
    const saveVote1PetrolBtn = $("#saveVote1PetrolBtn");
    const saveVote2PetrolBtn = $("#saveVote2PetrolBtn");

    const searchPromotionsBtn = $("#searchPromotionsBtn");
    const promotionsTable = $("#promotionsTable");

    async function fetchGetVehicleData() {

        const vehicleData = await myFetch("/api/this/getVehicleData");

        editVehicleFuelLeft.val(vehicleData.fuelLeft);
        editVehicleFuelPerKilometer.val(vehicleData.fuelPerKilometer);
        editVehicleLatitude.val(vehicleData.latitude);
        editVehicleLongitude.val(vehicleData.longitude);
    }

    async function fetchSaveVehicleData() {
        await myFetch("/api/this/setVehicleData", {
            fuelLeft: editVehicleFuelLeft.val(),
            fuelPerKilometer: editVehicleFuelPerKilometer.val(),
            latitude: editVehicleLatitude.val(),
            longitude: editVehicleLongitude.val()
        });
    }

    async function searchNearPetrolStations() {
        const stations = await myFetch("/api/this/findNearPetrolStation", {
            radius: radius.val()
        }) ?? [];

        nearPetrolStationsTable.empty();

        let i = 0;
        for (const station of stations) {

            const editButton = $("<button>", {
                text: "Pokaż",
                type: "button",
                class: "btn btn-primary",
                click: () => openVotePetrolStationModal(station)
            });

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: station.stationDescription.commonName}),
                    $("<td>", {text: station.stationDescription.latitude + "/" + station.stationDescription.longitude}),
                    $("<td>", {append: JSON.stringify(station.petrolPrice)}),
                    $("<td>", {append: [editButton]})
                ],
                appendTo: nearPetrolStationsTable
            });
        }
    }

    function openVotePetrolStationModal(station) {
        votePetrolStationModal.modal("show");

        votePetrolUniqueName.val(station.uniqueName)
        votePetrolPb95.val(station.petrolPrice.pb95);
        votePetrolPb98.val(station.petrolPrice.pb98);
        votePetrolDiesel.val(station.petrolPrice.diesel);
    }

    function saveVote1Petrol() {
        myFetch("/api/this/saveVote1Petrol", {
            uniqueName: votePetrolUniqueName.val(),
            petrolPrice: {
                pb95: votePetrolPb95.val(),
                pb98: votePetrolPb98.val(),
                diesel: votePetrolDiesel.val(),
            }
        })
    }

    function saveVote2Petrol() {
        myFetch("/api/this/saveVote2Petrol", {
            uniqueName: votePetrolUniqueName.val(),
            petrolPrice: {
                pb95: votePetrolPb95.val(),
                pb98: votePetrolPb98.val(),
                diesel: votePetrolDiesel.val(),
            }
        })
    }

    async function searchCheapestPetrolStations() {
        const station = await myFetch("/api/this/findCheapestPetrolStation") ?? [];
        nearPetrolStationsTable.empty();
        $("<tr>", {
            append: [
                $("<td>", {text: '1'}),
                $("<td>", {text: station.stationDescription.commonName}),
                $("<td>", {text: station.stationDescription.latitude + "/" + station.stationDescription.longitude}),
                $("<td>", {append: JSON.stringify(station.petrolPrice)}),
            ],
            appendTo: nearPetrolStationsTable
        });
    }

    async function reservePromotion(partnerUniqueName, promotionId) {
        var result = await myFetch("/api/this/reservePromotion", {
            partner: partnerUniqueName,
            promotionId: promotionId,
        });
        if (result)
            makeAlert({text: "Zarezerwowano", type: "success"});
        else
            makeAlert({text: "Błąd rezerwacji", type: "error"});
    }

    async function searchPromotions() {
        const patnersPromotions = await myFetch("/api/this/findPromotions") ?? [];
        promotionsTable.empty();

        let i = 0;
        for (const partner of patnersPromotions) {
            for (const promotion of partner.promotions) {
                const reserveButton = $("<button>", {
                    text: "Rezerwuj",
                    type: "button",
                    class: "btn btn-primary",
                    click: () => reservePromotion(partner.uniqueName, promotion.id)
                });

                $("<tr>", {
                    append: [
                        $("<td>", {text: ++i}),
                        $("<td>", {text: partner.uniqueName}),
                        $("<td>", {text: promotion.id}),
                        $("<td>", {append: promotion.description}),
                        $("<td>", {text: promotion.userIds.length + '/' + promotion.maxReservations}),
                        $("<td>", {append: [reserveButton]})
                    ],
                    appendTo: promotionsTable
                });
            }
        }
    }

    saveVehicleData.click(fetchSaveVehicleData);
    searchNearPetrolStationsBtn.click(searchNearPetrolStations);
    searchPromotionsBtn.click(searchPromotions);
    searchCheapestPetrolStationsBtn.click(searchCheapestPetrolStations);
    saveVote1PetrolBtn.click(saveVote1Petrol);
    saveVote2PetrolBtn.click(saveVote2Petrol);

    fetchGetVehicleData();
});