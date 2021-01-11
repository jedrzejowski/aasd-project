$(() => {
    const editVehicleFuelLeft = $("#editVehicleFuelLeft");
    const editVehicleFuelPerKilometer = $("#editVehicleFuelPerKilometer");
    const editVehicleLatitude = $("#editVehicleLatitude");
    const editVehicleLongitude = $("#editVehicleLongitude");
    const saveVehicleData = $("#saveVehicleData");

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

    saveVehicleData.click(fetchSaveVehicleData);

    fetchGetVehicleData();
});