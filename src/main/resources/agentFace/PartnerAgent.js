$(() => {
    const promotionsSearchBtn = $("#promotionsSearchBtn");
    const promotionsTbody = $("#promotionsTbody");

    const addPromotionShowModal = $("#addPromotionShowModal");
    const addPromotionModal = $("#addPromotionModal").modal({});
    const addPromotionAccept = $("#addPromotionAccept");
    const addPromotionName = $("#addPromotionName");
    const addPromotionDescription = $("#addPromotionDescription");
    const addPromotionNumber = $("#addPromotionNumber");

    function openAddPromotionModal() {
        addPromotionName.val("");
        addPromotionDescription.val("");
        addPromotionNumber.val("");
        addPromotionModal.modal("show");
    }

    function closePromotionModal() {
        addPromotionModal.modal("hide");
    }

    async function createNewPromotion() {
        const name = addPromotionName.val();
        const description = addPromotionDescription.val();
        const maxReservations = addPromotionNumber.val();

        try {
            await myFetch("/api/this/createPromotion", {
                id: name,
                description: description,
                maxReservations: maxReservations
            });

            makeAlert({text: "Utworzono promocję", type: "success"});
        } catch (e) {
            makeAlert({text: "Błąd tworzenia promocji", type: "error"});
        }

        closePromotionModal();
    }

    async function searchOwnedPromotions() {
        promotionsTbody.empty();

        const promotions = await myFetch("/api/this/getPromotions") ?? [];
        let i = 0;

        for (const promotion of promotions) {

            const editButton = $("<button>", {
                text: "Edytuj",
                type: "button",
                class: "btn btn-primary",
                click: () => openEditPromotionModal(promotion)
            })

            $("<tr>", {
                append: [
                    $("<td>", {text: ++i}),
                    $("<td>", {text: promotion.id}),
                    $("<td>", {append: promotion.description}),
                    $("<td>", {text: promotion.userIds.length + '/' + promotion.maxReservations}),
                    $("<td>", {append: [editButton]})
                ],
                appendTo: promotionsTbody
            });

        }
    }

    function openEditPromotionModal(promotion){
        addPromotionName.val(promotion.id);
        addPromotionDescription.val(promotion.description);
        addPromotionNumber.val(promotion.maxReservations);
        addPromotionModal.modal("show");
    }

    promotionsSearchBtn.click(searchOwnedPromotions);
    addPromotionShowModal.click(openAddPromotionModal);
    addPromotionAccept.click(createNewPromotion);
});