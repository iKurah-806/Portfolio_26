"use strict";
const roomList = document.getElementById("list");
const sendButton = document.getElementById("send");

roomList.addEventListener("change", () => {
  const anySelected = Array.from(roomList.options).some((opt) => opt.selected);
  if (anySelected) {
    sendButton.disabled = false;
  } else {
    sendButton.disabled = true;
  }
});

(function applyInitialTheme() {
  const body = document.body;

  const userId = body.getAttribute("data-user-id");

  const modeKey = userId ? `swack-mode_${userId}` : "swack-mode";
  const themeKey = userId ? `swack-theme_${userId}` : "swack-theme";

  console.log("参照するキー:", modeKey);

  const savedMode = localStorage.getItem(modeKey) || "light";
  const savedTheme = localStorage.getItem(themeKey) || "default";

  if (savedMode === "dark") {
    body.setAttribute("data-mode", "dark");
  } else {
    // ライトモード
    body.removeAttribute("data-mode");
    body.setAttribute("data-mode", "light");
  }

  if (savedTheme !== "default") {
    body.setAttribute("data-theme", savedTheme);
  } else {
    body.removeAttribute("data-theme");
  }
})();
