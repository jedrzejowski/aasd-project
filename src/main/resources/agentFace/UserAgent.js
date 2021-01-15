$(() => {
    const editVehicleFuelLeft = $("#editVehicleFuelLeft");
    const editVehicleFuelPerKilometer = $("#editVehicleFuelPerKilometer");
    const editVehicleLatitude = $("#editVehicleLatitude");
    const editVehicleLongitude = $("#editVehicleLongitude");
    const saveVehicleData = $("#saveVehicleData");
    const searchNearPetrolStationsBtn = $("#searchNearPetrolStationsBtn");
    const nearPetrolStationsTable = $("#nearPetrolStationsTable");
    const radius = $("#radius");

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

    saveVehicleData.click(fetchSaveVehicleData);
    searchNearPetrolStationsBtn.click(searchNearPetrolStations);

    fetchGetVehicleData();
});