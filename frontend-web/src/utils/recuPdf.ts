import { jsPDF } from 'jspdf';
import type { Paiement } from '@/api/services';

const VERT: [number, number, number] = [27, 94, 67];
const GRIS: [number, number, number] = [90, 90, 90];

const MOYENS: Record<string, string> = {
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  CARTE: 'Carte bancaire',
};

const formaterMontant = (montant?: number) =>
  montant != null ? `${montant.toLocaleString('fr-FR')} FCFA` : '—';

const formaterDate = (iso?: string) =>
  iso ? new Date(iso).toLocaleString('fr-FR', { dateStyle: 'long', timeStyle: 'short' }) : '—';

/**
 * Produit un reçu PDF pour un paiement de frais de visite, puis le télécharge.
 *
 * Le reçu existait à l'écran mais restait éphémère : impossible de le conserver
 * ou de le présenter comme preuve hors de l'application. Cette fonction le matérialise
 * en un document autonome, généré côté navigateur — sans charge serveur ni dépendance
 * supplémentaire côté back.
 *
 * Le libellé « démonstration » figure sur le document : la passerelle étant simulée,
 * le reçu ne doit pas laisser croire à un débit réel.
 */
export function telechargerRecuPaiement(paiement: Paiement): void {
  const doc = new jsPDF({ unit: 'mm', format: 'a4' });
  const largeur = doc.internal.pageSize.getWidth();
  const marge = 20;

  // En-tête
  doc.setFillColor(...VERT);
  doc.rect(0, 0, largeur, 32, 'F');
  doc.setTextColor(255, 255, 255);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(22);
  doc.text('ColocImmo', marge, 20);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(11);
  doc.text('Reçu de frais de visite', largeur - marge, 20, { align: 'right' });

  // Montant, mis en avant
  let y = 52;
  doc.setTextColor(...VERT);
  doc.setFont('helvetica', 'bold');
  doc.setFontSize(26);
  doc.text(formaterMontant(paiement.montant), largeur / 2, y, { align: 'center' });
  doc.setTextColor(...GRIS);
  doc.setFont('helvetica', 'normal');
  doc.setFontSize(11);
  y += 8;
  doc.text('Somme placée en séquestre', largeur / 2, y, { align: 'center' });

  // Séparateur
  y += 10;
  doc.setDrawColor(220, 220, 220);
  doc.line(marge, y, largeur - marge, y);

  // Détails, sous forme de lignes libellé / valeur
  const lignes: [string, string][] = [
    ['Référence', paiement.reference ?? '—'],
    ['Moyen de paiement', MOYENS[paiement.moyen ?? ''] ?? paiement.moyen ?? '—'],
    ['Statut', paiement.statut ?? '—'],
    ['Date', formaterDate(paiement.dateCreation)],
  ];
  if (paiement.annonceTitre) lignes.push(['Annonce', paiement.annonceTitre]);

  y += 12;
  doc.setFontSize(11);
  lignes.forEach(([libelle, valeur]) => {
    doc.setTextColor(...GRIS);
    doc.text(libelle, marge, y);
    doc.setTextColor(30, 30, 30);
    doc.text(String(valeur), largeur - marge, y, { align: 'right' });
    y += 9;
  });

  // Note de séquestre
  y += 6;
  doc.setDrawColor(220, 220, 220);
  doc.line(marge, y, largeur - marge, y);
  y += 10;
  doc.setTextColor(...GRIS);
  doc.setFontSize(10);
  const note =
    "Conservez ce reçu : la référence sert de preuve en cas de contestation. " +
    'La somme reste en séquestre et n\'est versée au démarcheur qu\'après la visite, ' +
    'ou vous est remboursée si elle n\'a pas lieu.';
  doc.text(doc.splitTextToSize(note, largeur - 2 * marge), marge, y);

  // Mention démonstration : la passerelle est simulée.
  y += 24;
  doc.setTextColor(150, 150, 150);
  doc.setFontSize(8);
  doc.text(
    'Document généré par ColocImmo. Passerelle de paiement simulée — aucun montant réel n\'est débité.',
    largeur / 2,
    y,
    { align: 'center' },
  );

  const nom = `recu-${paiement.reference ?? paiement.id ?? 'colocimmo'}.pdf`;
  doc.save(nom);
}
