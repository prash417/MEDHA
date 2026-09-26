const path = require("path");
const tools = path.join(process.env.APPDATA, "npm/node_modules/firebase-tools/lib");
const { configstore } = require(path.join(tools, "configstore"));
const { getAccessToken } = require(path.join(tools, "auth"));
const scopes = require(path.join(tools, "scopes"));

async function main() {
  const saved = configstore.get("tokens");
  const token = (await getAccessToken(saved.refresh_token, [scopes.CLOUD_PLATFORM])).access_token;
  let pageToken = "";
  let count = 0;
  do {
    const url = new URL(`https://storage.googleapis.com/storage/v1/b/paras-medha.firebasestorage.app/o`);
    url.searchParams.set("prefix", "voice/");
    url.searchParams.set("maxResults", "1000");
    if (pageToken) url.searchParams.set("pageToken", pageToken);
    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}`, "x-goog-user-project": "paras-medha" },
    });
    if (!res.ok) throw new Error(await res.text());
    const json = await res.json();
    count += (json.items || []).length;
    pageToken = json.nextPageToken || "";
    console.log("page", (json.items || []).length, "total", count);
  } while (pageToken);
  console.log("voice files", count);
}
main().catch((e) => { console.error(e); process.exit(1); });
