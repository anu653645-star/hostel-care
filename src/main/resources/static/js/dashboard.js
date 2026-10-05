(() => {
    const complaintList = document.getElementById("complaint-list");
    const searchInput = document.getElementById("complaint-search");
    const filterButtons = document.querySelectorAll(".filter-tab");
    const visibleCount = document.getElementById("visible-count");
    const countLabel = document.getElementById("count-label");
    const filterEmpty = document.getElementById("filter-empty");
    const cards = complaintList ? [...complaintList.querySelectorAll(".complaint-card")] : [];
    const statTotal = document.getElementById("stat-total");
    const statActive = document.getElementById("stat-active");
    const statSolved = document.getElementById("stat-solved");
    let activeFilter = "all";

    if (statTotal && statActive && statSolved) {
        statTotal.textContent = cards.length;
        statActive.textContent = cards.filter((card) => card.dataset.status !== "SOLVED").length;
        statSolved.textContent = cards.filter((card) => card.dataset.status === "SOLVED").length;
    }

    if (complaintList && searchInput) {
        function updateComplaints() {
            const query = searchInput.value.trim().toLocaleLowerCase();
            let visible = 0;

            cards.forEach((card) => {
                const isSolved = card.dataset.status === "SOLVED";
                const matchesFilter = activeFilter === "all"
                    || (activeFilter === "solved" && isSolved)
                    || (activeFilter === "active" && !isSolved);
                const matchesSearch = card.textContent.toLocaleLowerCase().includes(query);
                const shouldShow = matchesFilter && matchesSearch;

                card.hidden = !shouldShow;
                if (shouldShow) visible += 1;
            });

            visibleCount.textContent = visible;
            countLabel.textContent = visible === 1 ? "request" : "requests";
            filterEmpty.hidden = visible !== 0;
        }

        searchInput.addEventListener("input", updateComplaints);
        filterButtons.forEach((button) => {
            button.addEventListener("click", () => {
                activeFilter = button.dataset.filter;
                filterButtons.forEach((item) => {
                    const selected = item === button;
                    item.classList.toggle("is-active", selected);
                    item.setAttribute("aria-pressed", String(selected));
                });
                updateComplaints();
            });
        });

        document.querySelectorAll("[data-filter-link]").forEach((button) => {
            button.addEventListener("click", () => {
                const targetFilter = [...filterButtons].find((item) => item.dataset.filter === button.dataset.filterLink);
                if (targetFilter) {
                    targetFilter.click();
                    document.getElementById("requests").scrollIntoView({ behavior: "smooth", block: "start" });
                }
            });
        });

        document.addEventListener("keydown", (event) => {
            if (event.key === "/" && !event.ctrlKey && !event.metaKey && !event.altKey
                && !["INPUT", "TEXTAREA"].includes(document.activeElement.tagName)) {
                event.preventDefault();
                searchInput.focus();
            }
        });
    }

    const categorySelect = document.getElementById("category");
    const categoryTiles = document.querySelectorAll("[data-category-choice]");
    if (categorySelect && categoryTiles.length) {
        function syncCategoryTiles() {
            categoryTiles.forEach((tile) => {
                const selected = tile.dataset.categoryChoice === categorySelect.value;
                tile.classList.toggle("is-selected", selected);
                tile.setAttribute("aria-pressed", String(selected));
            });
        }

        categoryTiles.forEach((tile) => {
            tile.addEventListener("click", () => {
                categorySelect.value = tile.dataset.categoryChoice;
                syncCategoryTiles();
                document.getElementById("raise-request").scrollIntoView({ behavior: "smooth", block: "start" });
                categorySelect.focus({ preventScroll: true });
            });
        });
        categorySelect.addEventListener("change", syncCategoryTiles);
        syncCategoryTiles();
    }

    const navLinks = document.querySelectorAll(".nav-link[href^='#']");
    const navSections = [...navLinks]
        .map((link) => document.querySelector(link.getAttribute("href")))
        .filter(Boolean);
    if ("IntersectionObserver" in window && navSections.length) {
        const sectionObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                if (!entry.isIntersecting) return;
                navLinks.forEach((link) => {
                    const selected = link.getAttribute("href") === `#${entry.target.id}`;
                    link.classList.toggle("is-current", selected);
                });
            });
        }, { rootMargin: "-25% 0px -65% 0px" });
        navSections.forEach((section) => sectionObserver.observe(section));
    }

    const photoInput = document.getElementById("photo");
    const uploadZone = document.getElementById("upload-zone");
    const preview = document.getElementById("upload-preview");
    const photoHint = document.getElementById("photo-hint");
    if (photoInput && uploadZone && preview && photoHint) {
        let previewUrl;

        function showSelectedPhoto(file) {
            if (previewUrl) URL.revokeObjectURL(previewUrl);
            if (!file) {
                preview.hidden = true;
                preview.removeAttribute("src");
                photoHint.textContent = "A quick photo helps us understand the problem.";
                return;
            }

            if (!file.type.startsWith("image/")) {
                preview.hidden = true;
                photoHint.textContent = "That file doesn’t look like an image. Choose a JPG, PNG or WEBP.";
                return;
            }

            previewUrl = URL.createObjectURL(file);
            preview.src = previewUrl;
            preview.hidden = false;
            photoHint.textContent = `${file.name} · ${(file.size / (1024 * 1024)).toFixed(1)} MB`;
        }

        photoInput.addEventListener("change", () => showSelectedPhoto(photoInput.files[0]));
        ["dragenter", "dragover"].forEach((eventName) => {
            uploadZone.addEventListener(eventName, (event) => {
                event.preventDefault();
                uploadZone.classList.add("is-dragging");
            });
        });
        ["dragleave", "drop"].forEach((eventName) => {
            uploadZone.addEventListener(eventName, (event) => {
                event.preventDefault();
                uploadZone.classList.remove("is-dragging");
            });
        });
        uploadZone.addEventListener("drop", (event) => {
            const [file] = event.dataTransfer.files;
            if (!file) return;
            const transfer = new DataTransfer();
            transfer.items.add(file);
            photoInput.files = transfer.files;
            showSelectedPhoto(file);
        });
    }

    const photoDialog = document.getElementById("photo-dialog");
    const dialogPhoto = document.getElementById("dialog-photo");
    if (photoDialog && dialogPhoto) {
        document.querySelectorAll(".photo-button").forEach((button) => {
            button.addEventListener("click", () => {
                dialogPhoto.src = button.querySelector("img").src;
                photoDialog.showModal();
            });
        });
        photoDialog.querySelector(".dialog-close").addEventListener("click", () => photoDialog.close());
        photoDialog.addEventListener("click", (event) => {
            if (event.target === photoDialog) photoDialog.close();
        });
    }
})();
