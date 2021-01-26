// myFetch("api/pylon/stationDescription").then(stationName => {
//     console.log(stationName);
// });
$(() => {

    const editPetrolPb95 = $("#editPetrolPb95");
    const editPetrolPb98 = $("#editPetrolPb98");
    const editPetrolDiesel = $("#editPetrolDiesel");
    const refreshPetrolBtn = $("#refreshPetrolBtn");
    const savePetrolBtn = $("#savePetrolBtn");

    async function refreshPetrolPrice() {
        const petrolPrice = await myFetch("/api/this/getPetrolPrice")

        editPetrolPb95.val(petrolPrice.pb95);
        editPetrolPb98.val(petrolPrice.pb98);
        editPetrolDiesel.val(petrolPrice.diesel);
    }

    function savePetrolPrice() {
        myFetch("/api/this/setPetrolPrice", {
            pb95: editPetrolPb95.val(),
            pb98: editPetrolPb98.val(),
            diesel: editPetrolDiesel.val()
        })
    }

    savePetrolBtn.click(savePetrolPrice);
    refreshPetrolBtn.click(refreshPetrolPrice);

    refreshPetrolPrice();
});
