"use strict";

document.addEventListener("DOMContentLoaded", () => {
  // モーダル要素
  const modal = document.getElementById("myModal");
  if (!modal) return;

  // モーダルが開いたときに初期化
  modal.addEventListener("shown.bs.modal", () => {
    const form = modal.querySelector("#addMember_form");
    const input = modal.querySelector("#userNameInput");
    const container = modal.querySelector("#chipInputContainer");
    const userItems = modal.querySelectorAll("#userList .user-item");

    if (!form || !input || !container) return;

    // ====== 入力でリストを絞り込み ======
    input.addEventListener("input", () => {
      const filter = input.value.toLowerCase();
      userItems.forEach((item) => {
        const mail = item.querySelector(".user-name").textContent.toLowerCase();
        if (item.dataset.selected === "true") {
          item.style.display = "none";
        } else {
          item.style.display = mail.includes(filter) ? "" : "none";
        }
      });
    });

    // ====== Enterでチップ化 ======
    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        addChipFromInput();
      }
    });

    // ====== クリックでチップ化 ======
    userItems.forEach((item) => {
      item.addEventListener("click", () => {
        const mail = item.querySelector(".user-name").textContent.trim();
        addChip(mail, item);
      });
    });

    // 入力値からチップ化
    function addChipFromInput() {
      const value = input.value.trim();
      if (!value) return;

      const matchedItem = [...userItems].find(
        (item) =>
          item.querySelector(".user-name").textContent.trim().toLowerCase() === value.toLowerCase()
      );
      if (matchedItem) {
        addChip(value, matchedItem);
        input.value = "";
      }
    }

    // チップと hidden input を追加
    function addChip(mail, itemElement) {
      if (form.querySelector(`input[name="inviteMails"][value="${mail}"]`)) {
        return; // 重複
      }

      // チップ作成
      const chip = document.createElement("div");
      chip.className = "chip";
      chip.innerHTML = `${mail} <span class="chip-close">×</span>`;

      // hidden input 作成
      const hidden = document.createElement("input");
      hidden.type = "hidden";
      hidden.name = "inviteMails";
      hidden.value = mail;
      form.appendChild(hidden);

      // × ボタンで削除
      chip.querySelector(".chip-close").addEventListener("click", () => {
        chip.remove();
        hidden.remove();
        if (itemElement) {
          itemElement.dataset.selected = "false";
          itemElement.style.display = "";
        }

        // チップが0個になったらrequiredを有効化
        updateRequiredState();
      });

      container.insertBefore(chip, input);

      // 選択したユーザーをリストから非表示
      if (itemElement) {
        itemElement.dataset.selected = "true";
        itemElement.style.display = "none";
      }

      input.value = "";

      // チップが追加されたらrequiredを無効化
      updateRequiredState();
    }

    // required属性の状態を更新
    function updateRequiredState() {
      const selectedCount = form.querySelectorAll('input[name="inviteMails"]').length;

      if (selectedCount > 0) {
        // チップがある場合：入力フィールドのrequiredを無効化
        input.removeAttribute("required");
        input.placeholder = "追加で検索...";
      } else {
        // チップがない場合：入力フィールドのrequiredを有効化
        input.setAttribute("required", "required");
        input.placeholder = "ユーザー名を入力";
      }
    }

    // フォーム送信時の処理
    form.addEventListener("submit", (e) => {
      const selectedCount = form.querySelectorAll('input[name="inviteMails"]').length;

      // チップが選択されている場合は、入力フィールドを空にしてからrequiredを無効化
      if (selectedCount > 0) {
        input.value = "";
        input.removeAttribute("required"); // required無効化
        return true; // 送信を許可
      }

      // チップが選択されていない場合は、ブラウザのrequiredバリデーションが働く
      return true;
    });

    // 初期状態でrequiredを設定
    updateRequiredState();
  });
});
