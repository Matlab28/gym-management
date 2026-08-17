(() => {
    const forceLightMode = () => {
        document.documentElement.classList.remove("dark-mode");
        document.documentElement.style.colorScheme = "light";
    };

    forceLightMode();
    new MutationObserver(forceLightMode).observe(document.documentElement, {
        attributes: true,
        attributeFilter: ["class"]
    });
})();
