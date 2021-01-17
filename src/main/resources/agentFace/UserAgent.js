$(() => {
    const editVehicleFuelLeft = $("#editVehicleFuelLeft");
    const editVehicleFuelPerKilometer = $("#editVehicleFuelPerKilometer");
    const editVehicleLatitude = $("#editVehicleLatitude");
    const editVehicleLongitude = $("#editVehicleLongitude");
    const saveVehicleData = $("#saveVehicleData");
    const searchNearPetrolStationsBtn = $("#searchNearPetrolStationsBtn");
    const nearPetrolStationsTable = $("#nearPetrolStationsTable");
    const radius = $("#radius");

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
        const stations = await myFetch("/api/this/findNearPetrolStation",{
            radius: radius.val()
        }) ?? [];

        nearPetrolStationsTable.empty();

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
                appendTo: nearPetrolStationsTable
            });
        }
    }

    async function searchPromotions() {
        const pratnersPromotions = await myFetch("/api/this/findPromotions") ?? [];
        promotionsTable.empty();

        let i = 0;
        for (const partner of pratnersPromotions) {
            for (const promotion of partner.promotions) {
                const reserveButton = $("<button>", {
                    text: "Rezerwuj",
                    type: "button",
                    class: "btn btn-primary",
                    //click: () => openEditPetrolStationModal(stationUniqueName)
                });

                $("<tr>", {
                    append: [
                        $("<td>", {text: ++i}),
                        $("<td>", {text: partner.uniqueName}),
                        $("<td>", {text: promotion.id}),
                        $("<td>", {append: promotion.description}),
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

    fetchGetVehicleData();
});