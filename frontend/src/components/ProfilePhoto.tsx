import { useEffect, useState } from "react";
import { UserRound } from "lucide-react";
import { apiErrorMessage, uploadProfilePhoto } from "../lib/backend";

export function ProfileAvatar({ src, name }: { src?: string | null; name: string }) {
  const [failed, setFailed] = useState(false);
  useEffect(() => setFailed(false), [src]);
  return <span className="profile-avatar" role="img" aria-label={`Foto de ${name}`}>
    {src && !failed ? <img src={src} alt="" onError={() => setFailed(true)} /> : <UserRound size={30} aria-hidden="true" />}
  </span>;
}

export function ProfilePhotoField({ file, onChange, currentUrl }: {
  file: File | null; onChange: (file: File | null) => void; currentUrl?: string;
}) {
  const [preview, setPreview] = useState<string>();
  const [error, setError] = useState("");
  useEffect(() => {
    if (!file) { setPreview(undefined); return; }
    const url = URL.createObjectURL(file); setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [file]);
  return <div className="profile-photo-field">
    <ProfileAvatar src={preview || currentUrl} name="perfil" />
    <div>
      <label>Foto de perfil ou logo da loja (opcional)
        <input type="file" accept="image/jpeg,image/png" onChange={(event) => {
          const selected = event.target.files?.[0] || null;
          if (selected && (selected.size > 5 * 1024 * 1024 || !["image/jpeg", "image/png"].includes(selected.type))) {
            setError("Escolha JPEG ou PNG de até 5 MB."); event.target.value = ""; onChange(null); return;
          }
          setError(""); onChange(selected);
        }} />
      </label>
      <small>JPEG ou PNG, até 5 MB. Esta foto será pública nos seus anúncios.</small>
      {error && <p className="error-notice" role="alert">{error}</p>}
    </div>
  </div>;
}

export function ProfilePhotoEditor({ userId }: { userId: string }) {
  const [file, setFile] = useState<File | null>(null);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [revision, setRevision] = useState(0);
  return <form className="page-form page-panel profile-photo-editor" onSubmit={async (event) => {
    event.preventDefault(); if (!file) return;
    setSaving(true); setMessage("");
    try { await uploadProfilePhoto(userId, file); setRevision(Date.now()); setFile(null); setMessage("Foto atualizada."); }
    catch (error) { setMessage(apiErrorMessage(error, "Não foi possível salvar a foto.")); }
    finally { setSaving(false); }
  }}>
    <h2>Foto de perfil</h2>
    <ProfilePhotoField file={file} onChange={setFile} currentUrl={`/api/v1/perfis/${userId}/foto?v=${revision}`} />
    <button className="button primary" disabled={!file || saving}>{saving ? "Salvando…" : "Salvar foto"}</button>
    {message && <p role="status">{message}</p>}
  </form>;
}
