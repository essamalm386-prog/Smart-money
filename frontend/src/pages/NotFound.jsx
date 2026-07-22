import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="card mx-auto mt-10 max-w-md p-8 text-center">
      <Compass className="mx-auto h-10 w-10 text-slate-500" aria-hidden="true" />
      <h1 className="mt-3 text-2xl font-bold text-slate-100">Page introuvable</h1>
      <p className="mt-1 text-sm text-slate-400">Cette page n'existe pas dans Smart Money.</p>
      <Link to="/" className="btn-primary mt-5 inline-flex">
        Retour au tableau de bord
      </Link>
    </div>
  );
}
