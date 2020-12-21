$(() => {
    const searchMyPetrolStationButton = $("#searchMyPetrolStationButton");
    const addPetrolStationShowModal = $("#addPetrolStationShowModal");
    const addPetrolStationModal = $("#addPetrolStationModal").modal({});
    const addPetrolStationAccept = $("#addPetrolStationAccept");
    const addPetrolStationUniqueName = $("#addPetrolStationUniqueName");

    function openAddPetrolStationModal() {
        addPetrolStationModal.modal('show');
    }

    function createNewPetrolStation() {
        const uniqueName = addPetrolStationUniqueName.val();

        myFetch("/api/this/createPetrolStation", {
            uniqueName
        })
    }

    async function searchMyPetrolStation() {
        const owned = await myFetch("/api/this/ownedPetrolStation") ?? [];
        for (const stationUniqueName of owned) {

        }
    }

    searchMyPetrolStationButton.click(searchMyPetrolStation);
    addPetrolStationShowModal.click(openAddPetrolStationModal);
    addPetrolStationAccept.click(createNewPetrolStation);
});