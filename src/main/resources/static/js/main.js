"use strict";

// DOMが読み込まれたら、すべての処理を実行する
document.addEventListener("DOMContentLoaded", () => {
  // --- ローディング画面ランダム ---
  if (sessionStorage.getItem("fromLogin") === "true") {
    const overlay = document.getElementById("loadingOverlay");
    const messageEl = document.getElementById("loadingMessage");

    const messages = ["Now Loading...", "読み込み中..."];
    const randomMsg = messages[Math.floor(Math.random() * messages.length)];
    if (messageEl) messageEl.textContent = randomMsg;

    if (overlay) {
      overlay.style.display = "flex";
      setTimeout(() => {
        overlay.style.opacity = "0";
        setTimeout(() => {
          overlay.style.display = "none";
          overlay.style.opacity = "1";
          sessionStorage.removeItem("fromLogin");
        }, 500);
      }, 1000);
    }
  }

  // --- メッセージ送信関連 ---
  const elSendButton = document.getElementById("send");
  const elMessage = document.getElementById("message");
  const elMessageForm = document.getElementById("messageForm");
  const elLogArea = document.getElementById("logArea");
  const fileInputMsg = document.getElementById("fileInputMsg");

  if (elLogArea) elLogArea.scrollTop = elLogArea.scrollHeight + 110;

  if (elMessage) {
    elMessage.addEventListener("keydown", (e) => {
      if (e.ctrlKey && e.key === "Enter" && e.target.value) {
        if (elMessageForm) elMessageForm.submit();
        e.preventDefault();
      } else if (e.key === "Enter") {
        e.preventDefault();
      }
    });
    elMessage.addEventListener("input", updateSendButtonState);
  }

  function updateSendButtonState() {
    if (!elMessage || !elSendButton || !fileInputMsg) return;
    if ((elMessage.value.length === 0) && (fileInputMsg.files.length === 0)) {
      elSendButton.style.color = "rgba(44, 45, 48, 0.75)";
      elSendButton.style.backgroundColor = "#e8e8e8";
      elSendButton.disabled = true;
    } else {
      elSendButton.style.color = "#ffffff";
      elSendButton.style.backgroundColor = "#008952";
      elSendButton.disabled = false;
    }
  }
  updateSendButtonState();

  // --- ドロップダウン関連 ---
  // チャットログドロップダウン
  document.querySelectorAll(".dropdown-menu-btn").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      const id = btn.dataset.chatlogId;
      const dropdown = document.getElementById("dropdown-" + id);
      if (!dropdown) return;

      document.querySelectorAll(".dropdown-menu-content").forEach((menu) => {
        if (menu !== dropdown) menu.style.display = "none";
      });

      // トグル表示
      if (dropdown.style.display === "block") {
        dropdown.style.display = "none";
      } else {
        const rect = btn.getBoundingClientRect();
        dropdown.style.left = rect.left + "px";
        dropdown.style.top = rect.bottom + 2 + "px";
        dropdown.style.display = "block";
      }
    });
  });

  // チャット絵文字サブメニュー
  document.querySelectorAll(".dropdown-item.submenu").forEach((item) => {
    item.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();
      const id = item.dataset.chatlogId;
      const submenu = document.getElementById("emoji-submenu-" + id);
      if (!submenu) return;

      document.querySelectorAll(".dropdown-submenu-content").forEach((menu) => {
        if (menu !== submenu) menu.style.display = "none";
      });

      submenu.style.display = submenu.style.display === "block" ? "none" : "block";
    });
  });

  // 権限サブメニュー
  document.querySelectorAll(".dropdown-submenu > .dropdown-toggle").forEach((toggle) => {
    toggle.addEventListener("click", (e) => {
      e.preventDefault();
      e.stopPropagation();
      const submenu = toggle.nextElementSibling;
      if (!submenu) return;

      document.querySelectorAll(".dropdown-submenu > .dropdown-menu").forEach((menu) => {
        if (menu !== submenu) menu.style.display = "none";
      });

      submenu.style.display = submenu.style.display === "block" ? "none" : "block";
    });
  });

  // 親ドロップダウンが閉じられたらサブメニューも閉じる
  document.querySelectorAll(".dropdown").forEach((dropdown) => {
    dropdown.addEventListener("hidden.bs.dropdown", () => {
      dropdown.querySelectorAll(".dropdown-submenu > .dropdown-menu").forEach((menu) => {
        menu.style.display = "none";
      });
    });
  });

  // 画面クリックで全て閉じる
  document.addEventListener("click", () => {
    document
      .querySelectorAll(".dropdown-menu-content, .dropdown-submenu .dropdown-menu")
      .forEach((menu) => {
        menu.style.display = "none";
      });
  });

  // --- 最後に開いたタブを保存 / リダイレクト (ユーザー別) ---
  const path = window.location.pathname;
  const params = new URLSearchParams(window.location.search);
  const roomId = params.get("roomId");

  // ★ここが1回目の宣言 (これを正とする)
  const currentUserId = document.body.getAttribute("data-user-id");

  // メイン画面での処理 (ログインしている場合のみ)
  if (path === "/" && currentUserId) {
    // ユーザー固有のキーを作成 (例: lastRoom_U0001)
    const storageKey = `lastRoom_${currentUserId}`;

    if (roomId) {
      // URLに roomId がある場合 → 「このユーザーの」履歴として保存
      localStorage.setItem(storageKey, roomId);
    } else {
      // URLに roomId がない場合 (ログイン直後など) → 「このユーザーの」履歴を取得
      const last = localStorage.getItem(storageKey);

      // 履歴があればそこへ、なければデフォルト(R0000)へリダイレクト
      window.location.href = last ? `/?roomId=${last}` : "/?roomId=R0000";
    }
  }

  // メンバー追加エラー
  const errorMsg = document.getElementById("errorMsg");
  if (errorMsg && errorMsg.textContent.trim() !== "") {
    alert(errorMsg.textContent.trim());
  }

  // --- ドロワーの初期化 ---
  const profileButton = document.getElementById("profileButton");
  const drawerMenu = document.getElementById("drawerMenu");
  const drawerOverlay = document.getElementById("drawerOverlay");
  const closeDrawer = document.getElementById("closeDrawer");

  function openDrawer() {
    if (drawerMenu) drawerMenu.classList.add("show");
    if (drawerOverlay) drawerOverlay.classList.add("show");
    document.body.style.overflow = "hidden";
  }

  function closeDrawerMenu() {
    if (drawerMenu) drawerMenu.classList.remove("show");
    if (drawerOverlay) drawerOverlay.classList.remove("show");
    document.body.style.overflow = "";
  }

  if (profileButton) profileButton.addEventListener("click", openDrawer);
  if (closeDrawer) closeDrawer.addEventListener("click", closeDrawerMenu);
  if (drawerOverlay) drawerOverlay.addEventListener("click", closeDrawerMenu);

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && drawerMenu && drawerMenu.classList.contains("show")) {
      closeDrawerMenu();
    }
  });

  // --- パスワード変更モーダル処理 ---
  const passwordForm = document.getElementById("passwordChangeForm");
  const passwordMessage = document.getElementById("passwordChangeMessage");

  if (passwordForm) {
    passwordForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const oldPassword = document.getElementById("oldPassword").value;
      const newPassword = document.getElementById("newPassword").value;

      try {
        const response = await fetch("/api/users/change-password", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ oldPassword, newPassword }),
          credentials: "include",
        });

        if (response.status === 401 || response.status === 403) {
          if (passwordMessage) {
            passwordMessage.textContent = "認証エラー。再度ログインしてください。";
            passwordMessage.classList.remove("text-success");
            passwordMessage.classList.add("text-danger");
          }
          return;
        }

        const result = await response.json();

        if (response.ok) {
          if (passwordMessage) {
            passwordMessage.textContent = result.message || "パスワードを変更しました";
            passwordMessage.classList.remove("text-danger");
            passwordMessage.classList.add("text-success");
          }

          // ローカルストレージ更新
          try {
            const loginDataStr = localStorage.getItem("loginData");
            if (loginDataStr) {
              let loginData = JSON.parse(loginDataStr);
              if (loginData && loginData.hasOwnProperty("mailAddress")) {
                loginData.password = newPassword;
                localStorage.setItem("loginData", JSON.stringify(loginData));
              }
            }
          } catch (storageErr) {
            console.error("ローカルストレージの更新に失敗しました:", storageErr);
          }

          passwordForm.reset();
          setTimeout(() => {
            const modalEl = document.getElementById("passwordChangeModal");
            if (modalEl) {
              const modal = bootstrap.Modal.getInstance(modalEl);
              if (modal) modal.hide();
            }
            if (passwordMessage) passwordMessage.textContent = "";
          }, 1000);
        } else {
          if (passwordMessage) {
            passwordMessage.textContent = result.message || "パスワード変更に失敗しました";
            passwordMessage.classList.remove("text-success");
            passwordMessage.classList.add("text-danger");
          }
        }
      } catch (err) {
        console.error(err);
        if (passwordMessage) {
          passwordMessage.textContent = "通信エラーが発生しました";
          passwordMessage.classList.remove("text-success");
          passwordMessage.classList.add("text-danger");
        }
      }
    });
  }

  // --- サイドバーのドラッグ ---
  const resizer = document.querySelector(".resizer");
  const left = document.querySelector(".left");
  const container = document.querySelector(".container");

  let isResizing = false;

  if (resizer && left && container) {
    resizer.addEventListener("mousedown", (e) => {
      isResizing = true;
      document.body.style.cursor = "col-resize";
      e.preventDefault();
    });

    document.addEventListener("mousemove", (e) => {
      if (!isResizing) return;
      const newWidth = e.clientX - container.offsetLeft;
      if (newWidth >= 180 && newWidth <= 540) left.style.width = `${newWidth}px`;
    });

    document.addEventListener("mouseup", () => {
      if (isResizing) {
        isResizing = false;
        document.body.style.cursor = "default";
      }
    });
  }

  // --- ユーザ削除モーダル処理 ---
  const deleteUserModal = document.getElementById("deleteUserModal");
  const deleteUsersList = document.getElementById("deleteUsersList");
  const deleteAlert = document.getElementById("deleteAlert");
  const bulkDeleteButton = document.getElementById("bulkDeleteButton");
  const checkAllUsers = document.getElementById("checkAllUsers");

  function showAlert(message, type) {
    if (!deleteAlert) return;
    deleteAlert.textContent = message;
    deleteAlert.className = `alert alert-${type} mt-3`;
    deleteAlert.classList.remove("d-none");
  }

  async function loadUsers() {
    if (!deleteUsersList) return;
    if (checkAllUsers) checkAllUsers.checked = false;
    deleteUsersList.innerHTML =
      '<div class="text-center"><span class="spinner-border spinner-border-sm"></span> 読み込み中...</div>';

    try {
      const response = await fetch("/api/users", { credentials: "include" });
      if (response.status === 401) throw new Error("認証エラー (401)");
      if (!response.ok) throw new Error(`サーバーエラー (${response.status})`);

      const result = await response.json();
      let users;
      let sessionUserId = null; // 変数名を変更 (mainスコープのcurrentUserIdと衝突しないように)

      if (Array.isArray(result)) {
        users = result;
        sessionUserId = null;
      } else if (result && Array.isArray(result.users)) {
        users = result.users;
        sessionUserId = result.currentUserId;
      } else {
        throw new Error("サーバーから予期しない形式のユーザーデータが返されました。");
      }

      if (users.length === 0) {
        deleteUsersList.innerHTML =
          '<p class="text-center text-muted">退会可能なユーザーがいません。</p>';
        if (bulkDeleteButton) bulkDeleteButton.disabled = true;
        if (checkAllUsers) checkAllUsers.disabled = true;
        return;
      }

      if (bulkDeleteButton) bulkDeleteButton.disabled = false;
      if (checkAllUsers) checkAllUsers.disabled = false;

      deleteUsersList.innerHTML = users
        .map((user) => {
          const isSelf = user.userId === sessionUserId;
          const disabledAttr = isSelf ? "disabled" : "";
          const selfLabel = isSelf ? ' <span class="text-muted small">(自分)</span>' : "";
          const selfClass = isSelf ? "bg-light" : "";

          return `
          <label class="list-group-item list-group-item-action d-flex align-items-center ${selfClass}" id="user-card-${user.userId}">
            <input class="form-check-input me-3" type="checkbox" value="${user.userId}" data-user-id="${user.userId}" ${disabledAttr}>
            <h6 class="mb-0">${user.userName}${selfLabel}</h6>
          </label>
        `;
        })
        .join("");
    } catch (error) {
      console.error("ユーザーの読み込みに失敗しました:", error);
      deleteUsersList.innerHTML = `<p class="text-danger text-center">読み込み失敗: ${error.message}</p>`;
    }
  }

  if (deleteUserModal) {
    deleteUserModal.addEventListener("show.bs.modal", async () => {
      if (deleteAlert) {
        deleteAlert.classList.add("d-none");
        deleteAlert.textContent = "";
      }
      await loadUsers();
    });

    if (checkAllUsers && deleteUsersList) {
      checkAllUsers.addEventListener("change", (e) => {
        const isChecked = e.target.checked;
        deleteUsersList.querySelectorAll('input[type="checkbox"]').forEach((checkbox) => {
          checkbox.checked = isChecked;
        });
      });
    }

    if (bulkDeleteButton && deleteUsersList) {
      bulkDeleteButton.addEventListener("click", async () => {
        const selectedIds = Array.from(
          deleteUsersList.querySelectorAll('input[type="checkbox"]:checked')
        ).map((cb) => cb.value);

        if (selectedIds.length === 0) {
          showAlert("退会させるユーザーを選択してください。", "warning");
          return;
        }
        if (!confirm(`選択された ${selectedIds.length} 人のユーザーを本当に退会させますか？`))
          return;

        bulkDeleteButton.disabled = true;
        bulkDeleteButton.textContent = "処理中...";
        if (deleteAlert) deleteAlert.classList.add("d-none");

        try {
          const response = await fetch("/api/users/bulk-delete", {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ ids: selectedIds }),
            credentials: "include",
          });

          if (response.status === 401 || response.status === 403)
            throw new Error("認証エラー (401/403)");
          if (!response.ok) {
            let errorMsg = "サーバーでエラーが発生しました";
            try {
              const errResult = await response.json();
              if (errResult.message) errorMsg = errResult.message;
            } catch (e) {}
            throw new Error(errorMsg);
          }

          selectedIds.forEach((id) => {
            const userCard = document.getElementById(`user-card-${id}`);
            if (userCard) userCard.remove();
          });
          showAlert("選択されたユーザーを退会させました。", "success");

          if (deleteUsersList.querySelectorAll('input[type="checkbox"]').length === 0) {
            deleteUsersList.innerHTML =
              '<p class="text-center text-muted">退会可能なユーザーがいません。</p>';
            if (checkAllUsers) checkAllUsers.disabled = true;
            bulkDeleteButton.disabled = true;
          }
        } catch (error) {
          console.error("一括削除に失敗しました:", error);
          showAlert(`削除に失敗しました: ${error.message}`, "danger");
        } finally {
          bulkDeleteButton.disabled = false;
          bulkDeleteButton.textContent = "選択したユーザーを退会させる";
          if (checkAllUsers) checkAllUsers.checked = false;
        }
      });
    }
  }

  // --- アカウントロック解除モーダル処理 ---
  const lockedUsersModal = document.getElementById("lockedUsersModal");
  const lockedUsersListArea = document.getElementById("lockedUsersListArea");
  const unlockAlert = document.getElementById("unlockAlert");

  if (lockedUsersModal && lockedUsersListArea && unlockAlert) {
    lockedUsersModal.addEventListener("show.bs.modal", async () => {
      await loadLockedUsers();
    });

    async function loadLockedUsers() {
      lockedUsersListArea.innerHTML = '<div class="text-center p-3">読み込み中...</div>';
      showUnlockAlert("", "d-none");

      try {
        const response = await fetch("/api/users", { credentials: "include" });
        if (!response.ok) throw new Error("認証エラーまたはサーバーエラー");
        const result = await response.json();
        let users;
        if (Array.isArray(result)) users = result;
        else if (result && Array.isArray(result.users)) users = result.users;
        else throw new Error("予期しないデータ形式");

        const lockedUsers = users.filter((user) => {
          if (typeof user.locked === "boolean") return user.locked === true;
          if (typeof user.locked === "string") return user.locked.trim().toUpperCase() === "Y";
          return false;
        });

        lockedUsersListArea.innerHTML = "";
        if (lockedUsers.length === 0) {
          lockedUsersListArea.innerHTML =
            '<div class="text-center text-muted p-3">現在ロック中のユーザーはいません。</div>';
          return;
        }

        lockedUsers.forEach((user) => {
          const item = document.createElement("div");
          item.className = "list-group-item d-flex justify-content-between align-items-center";
          item.setAttribute("data-user-id", user.userId);
          const imgPath = user.userImgPath ? user.userImgPath : "/images/profile/profile.png";

          item.innerHTML = `
            <div class="d-flex align-items-center">
              <img src="${imgPath}" onerror="this.src='/images/profile/profile.png';" class="rounded me-3" style="width: 40px; height: 40px; object-fit: cover;">
              <div>
                <h6 class="mb-0">${user.userName}</h6>
                <small class="text-muted">${user.mailAddress}</small>
              </div>
            </div>
            <button class="btn btn-success btn-sm unlock-btn" data-user-id="${user.userId}">解除する</button>
          `;
          lockedUsersListArea.appendChild(item);
        });
      } catch (error) {
        console.error("ロック中ユーザーの読み込み失敗:", error);
        lockedUsersListArea.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
        showUnlockAlert(error.message, "alert-danger");
      }
    }

    lockedUsersListArea.addEventListener("click", async (e) => {
      if (e.target.classList.contains("unlock-btn")) {
        const button = e.target;
        const userIdToUnlock = button.getAttribute("data-user-id");
        button.disabled = true;
        button.textContent = "解除中...";
        await unlockUser(userIdToUnlock, button);
      }
    });

    async function unlockUser(userId, button) {
      try {
        const response = await fetch("/api/users/unlock", {
          method: "POST",
          headers: { "Content-Type": "application/x-www-form-urlencoded" },
          body: new URLSearchParams({ targetUserId: userId }),
          credentials: "include",
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || "解除処理に失敗しました。");

        showUnlockAlert(result.message, "alert-success");
        const itemToRemove = document.querySelector(
          `#lockedUsersListArea .list-group-item[data-user-id="${userId}"]`
        );
        if (itemToRemove) itemToRemove.remove();
        if (document.querySelectorAll("#lockedUsersListArea .list-group-item").length === 0) {
          lockedUsersListArea.innerHTML =
            '<div class="text-center text-muted p-3">ロック中のユーザーはいません。</div>';
        }
      } catch (error) {
        console.error("ロック解除失敗:", error);
        showUnlockAlert(error.message, "alert-danger");
        button.disabled = false;
        button.textContent = "解除する";
      }
    }

    function showUnlockAlert(message, className) {
      unlockAlert.textContent = message;
      unlockAlert.className = "alert mt-2";
      if (className === "d-none") unlockAlert.classList.add("d-none");
      else {
        unlockAlert.classList.add(className);
        unlockAlert.classList.remove("d-none");
      }
    }
  }

  // --- テーマ/モード設定の初期化 ---
  // キーを作成 (ログインしていない場合は共通キーを使う)
  const keyMode = currentUserId ? `swack-mode_${currentUserId}` : "swack-mode";
  const keyTheme = currentUserId ? `swack-theme_${currentUserId}` : "swack-theme";

  // カラーモードの呼び出し
  const savedMode = localStorage.getItem(keyMode) || "light";
  setMode(savedMode);

  // テーマカラーの呼び出し
  const savedTheme = localStorage.getItem(keyTheme) || "default";
  setTheme(savedTheme);

  // --- プロフィール変更のファイル選択 (移動・統合) ---
  const changeBtn = document.getElementById("changeBtn");
  const fileInput = document.getElementById("fileInput");
  const form = document.getElementById("uploadForm");

  // ボタン押したらファイル選択を開く
  changeBtn.addEventListener("click", function () {
    fileInput.click();
  });

  // ファイルが選ばれたら自動送信
  fileInput.addEventListener("change", function () {
    if (fileInput.files.length > 0) {
      form.submit();
    }
  });

  // 画像送信のファイル選択
  const uploadBtn = document.getElementById("uploadFile");
  const previewArea = document.getElementById("imagePreview");
  const previewImg = document.getElementById("previewImg");
  const removePreview = document.getElementById("removePreview");

  // 画像選択ボタン
  uploadBtn.addEventListener("click", function () {
    fileInputMsg.click();
  });

  // 画像が選ばれた時
  fileInputMsg.addEventListener("change", function () {
    if (fileInputMsg.files && fileInputMsg.files[0]) {
      const file = fileInputMsg.files[0];
      const url = URL.createObjectURL(file);

      previewImg.src = url;
      previewArea.style.display = "inline-block";
      updateSendButtonState();
    }
  });

  // プレビュー削除（選び直し）
  removePreview.addEventListener("click", function () {
    fileInputMsg.value = "";
    previewImg.src = "";
    previewArea.style.display = "none";
    updateSendButtonState();
  });

}); // --- DOMContentLoaded ここまで ---


// カラーモードを設定 (Light / Dark)
function setMode(mode) {
  const body = document.body;
  const userId = body.getAttribute("data-user-id");

  if (mode === "dark") {
    body.setAttribute("data-mode", "dark");
  } else {
    body.removeAttribute("data-mode");
  }
  const key = userId ? `swack-mode_${userId}` : "swack-mode";
  localStorage.setItem(key, mode);
  updateActiveModeBtn(mode);
}

// テーマカラーを設定 (サイドバーとヘッダー)
function setTheme(theme) {
  const body = document.body;
  const userId = body.getAttribute("data-user-id");

  if (theme === "default") {
    body.removeAttribute("data-theme");
  } else {
    body.setAttribute("data-theme", theme);
  }
  const key = userId ? `swack-theme_${userId}` : "swack-theme";
  localStorage.setItem(key, theme);
  updateActiveThemeBtn(theme);
}

function updateActiveModeBtn(mode) {
  const buttons = document.querySelectorAll('.theme-btn[onclick^="setMode"]');
  buttons.forEach((btn) => btn.classList.remove("active-theme"));

  const targetBtn = document.querySelector(`.theme-btn[onclick="setMode('${mode}')"]`);
  if (targetBtn) {
    targetBtn.classList.add("active-theme");
  }
}

function updateActiveThemeBtn(theme) {
  const buttons = document.querySelectorAll('.theme-btn[onclick^="setTheme"]');
  buttons.forEach((btn) => btn.classList.remove("active-theme"));

  const targetBtn = document.querySelector(`.theme-btn[onclick="setTheme('${theme}')"]`);
  if (targetBtn) {
    targetBtn.classList.add("active-theme");
  }
}

// ログアウト処理
function handleLogout() {
  localStorage.removeItem("loginData");
}

// 編集ボタンを押したとき
function startEdit(buttonElement) {
  const chatLogId = buttonElement.dataset.chatlogId;
  const displayMode = document.getElementById("display-mode-" + chatLogId);
  const editMode = document.getElementById("edit-mode-" + chatLogId);
  if (displayMode && editMode) {
    displayMode.classList.add("d-none");
    editMode.classList.remove("d-none");
  }
}

// キャンセルボタンを押したとき
function cancelEdit(buttonElement) {
  const chatLogId = buttonElement.dataset.chatlogId;
  const displayMode = document.getElementById("display-mode-" + chatLogId);
  const editMode = document.getElementById("edit-mode-" + chatLogId);
  if (displayMode && editMode) {
    editMode.classList.add("d-none");
    displayMode.classList.remove("d-none");
  }
}


// --- 権限付与モーダル処理 ---
  const grantAuthModal = document.getElementById("grantAuthorityModal");
  const authUserList = document.getElementById("authorityUserList");
  const authAlert = document.getElementById("authorityAlert");

  if (grantAuthModal && authUserList && authAlert) {
    // モーダルが開くときにリストを読み込む
    grantAuthModal.addEventListener("show.bs.modal", async () => {
      await loadAuthorityUsers();
    });

    // ユーザー一覧取得・表示
    async function loadAuthorityUsers() {
      authUserList.innerHTML = '<div class="text-center p-3">読み込み中...</div>';
      authAlert.classList.add("d-none");

      try {
        const response = await fetch("/api/users", { credentials: "include" });
        if (!response.ok) throw new Error("ユーザー情報の取得に失敗しました");

        const result = await response.json();
        let users = Array.isArray(result) ? result : (result.users || []);
        const myId = document.body.getAttribute("data-user-id");

        if (users.length === 0) {
          authUserList.innerHTML = '<div class="p-3">ユーザーがいません</div>';
          return;
        }

        authUserList.innerHTML = "";

        users.forEach(user => {
          const isMe = (user.userId === myId);
          const isAdmin = (user.role === 'ADMIN');

          const btnState = isMe ? 'disabled' : '';
          const btnClass = isAdmin ? 'btn-outline-danger' : 'btn-outline-primary';
          const btnText = isAdmin ? '一般に降格' : '管理者に昇格';
          const roleBadge = isAdmin
            ? '<span class="badge bg-danger">ADMIN</span>'
            : '<span class="badge bg-secondary">USER</span>';

          const item = document.createElement("div");
          item.className = "list-group-item d-flex justify-content-between align-items-center";
          item.innerHTML = `
            <div>
              <div class="fw-bold">${user.userName} ${isMe ? '(自分)' : ''}</div>
              <div class="small text-muted">${user.mailAddress}</div>
            </div>
            <div class="d-flex align-items-center gap-3">
              ${roleBadge}
              <button class="btn btn-sm ${btnClass} role-change-btn"
                data-user-id="${user.userId}"
                data-current-role="${user.role}" ${btnState}>
                ${btnText}
              </button>
            </div>
          `;
          authUserList.appendChild(item);
        });

      } catch (error) {
        authUserList.innerHTML = `<div class="text-danger p-3">${error.message}</div>`;
      }
    }

    // ボタンクリック時の処理 (権限変更)
    authUserList.addEventListener("click", async (e) => {
      if (e.target.classList.contains("role-change-btn")) {
        const btn = e.target;
        const userId = btn.getAttribute("data-user-id");
        const currentRole = btn.getAttribute("data-current-role");
        // ADMINならUSERへ、USERならADMINへ切り替え
        const newRole = (currentRole === 'ADMIN') ? 'USER' : 'ADMIN';

        if(!confirm(`このユーザーの権限を ${newRole} に変更しますか？`)) return;

        btn.disabled = true;

        try {
          const response = await fetch("/api/users/update-role", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId: userId, role: newRole }),
            credentials: "include",
          });

          if (!response.ok) throw new Error("更新に失敗しました");

          await loadAuthorityUsers();

          authAlert.textContent = "権限を変更しました";
          authAlert.className = "alert alert-success mt-3";
          authAlert.classList.remove("d-none");

          setTimeout(() => authAlert.classList.add("d-none"), 3000);

        } catch (error) {
          console.error(error);
          authAlert.textContent = "エラー: " + error.message;
          authAlert.className = "alert alert-danger mt-3";
          authAlert.classList.remove("d-none");
          btn.disabled = false;
        }
      }
    });
  }
