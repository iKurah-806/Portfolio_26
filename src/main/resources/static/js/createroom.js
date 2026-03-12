"use strict";

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

document.addEventListener("DOMContentLoaded", function () {
  const myCheck = document.getElementById("chk");
  const checkLabel = document.getElementById("check_label");
  const checkText = document.getElementById("check_text");
  const sendButton = document.getElementById("send");
  const roomName = document.getElementById("name");

  // プライベート/パブリック切り替え
  if (myCheck) {
    myCheck.addEventListener("change", () => {
      if (myCheck.checked) {
        checkLabel.textContent = "プライベート";
        checkText.textContent = "このルームは、招待によってのみ参加または確認することができます。";
      } else {
        checkLabel.textContent = "パブリック";
        checkText.textContent =
          "このルームは、ワークスペースのメンバーであれば誰でも閲覧・参加することができます。";
      }
    });
  }

  // ルーム名入力チェック
  if (roomName) {
    roomName.addEventListener("input", () => {
      if (roomName.value.trim().length > 0) {
        sendButton.disabled = false;
      } else {
        sendButton.disabled = true;
      }
    });
  }

  // --- 以下、チップ化ロジック ---
  const input = document.getElementById("userNameInput");
  const container = document.getElementById("chipInputContainer");
  const form = document.getElementById("addMember_form");
  const userItems = document.querySelectorAll("#userList .user-item");

  if (!input) return;

  // 入力でリストを絞り込み
  input.addEventListener("input", () => {
    const filter = input.value.toLowerCase();
    userItems.forEach((item) => {
      // user-nameクラスを持つ要素を探す
      const nameEl = item.querySelector(".user-name");
      if (!nameEl) return;

      const mail = nameEl.textContent.toLowerCase();

      if (item.dataset.selected === "true") {
        item.style.display = "none";
      } else {
        item.style.display = mail.includes(filter) ? "" : "none";
      }
    });
  });

  // Enterでチップ化
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      addChipFromInput();
    }
  });

  // クリックでチップ化
  userItems.forEach((item) => {
    item.addEventListener("click", () => {
      const nameEl = item.querySelector(".user-name");
      if (nameEl) {
        const mail = nameEl.textContent.trim();
        addChip(mail, item);
      }
    });
  });

  function addChipFromInput() {
    const value = input.value.trim();
    if (!value) return;

    const matchedItem = [...userItems].find((item) => {
      const nameEl = item.querySelector(".user-name");
      return nameEl && nameEl.textContent.trim().toLowerCase() === value.toLowerCase();
    });

    if (matchedItem) {
      addChip(value, matchedItem);
      input.value = "";
    }
  }

  function addChip(mail, itemElement) {
    // 重複チェック
    if (document.querySelector(`input[name="inviteMails"][value="${mail}"]`)) {
      return;
    }

    // チップ作成
    const chip = document.createElement("div");
    chip.className = "chip";
    chip.innerHTML = `
      ${mail}
      <span class="chip-close">×</span>
    `;

    // hidden input作成
    const hidden = document.createElement("input");
    hidden.type = "hidden";
    hidden.name = "inviteMails";
    hidden.value = mail;

    form.appendChild(hidden);

    // 削除ボタンイベント
    chip.querySelector(".chip-close").addEventListener("click", () => {
      chip.remove();
      hidden.remove();

      if (itemElement) {
        itemElement.dataset.selected = "false";
        itemElement.style.display = "";
      }
    });

    container.insertBefore(chip, input);

    if (itemElement) {
      itemElement.dataset.selected = "true";
      itemElement.style.display = "none";
    }

    input.value = "";
  }
});
