# Petpulse — Market Card & Multi-Photo Listings

Built on top of commit 02300dc (the INR currency fix). 5 files changed.

## 1. Price moved from the cramped right side to the left
In `MarketplaceScreen.kt` (MarketPetCard):
- The price used to sit in a narrow right-hand column that got squeezed to
  near-zero width by long breed text, wrapping "₹34500" into stacked characters.
- Now the text column takes the available width (weight 1f) and the price sits
  left-aligned directly under the breed/age/gender line, with the old
  strike-through price beside it. Adoption listings show "FREE" in the same place.

## 2. Multiple photos per listing (for better sales)
- `MarketplaceModels.kt`: MarketPet now has photoUris: List<String> (replaces an
  unused single imageUrl field).
- `MarketplaceModals.kt` (ListPetFormModal): new "Add Photos" button opens the
  Android photo picker, up to 5 images. Selected photos show as a thumbnail strip;
  tap a thumbnail to remove it. Counter shows "n/5 selected".
- `MarketplaceScreen.kt`: the market card shows the first photo as the circular
  avatar (falls back to the species emoji when there are no photos) and a strip of
  the remaining photos below the title area.
- `MainActivity.kt` + `PetViewModel.kt`: photos flow from the form through the view
  model into the MarketPet listing.

## How to apply
Same procedure as the INR fix: copy the `app/` folder over your repo root,
commit, push. The next build will produce the new APK.

## Notes
- Photos are held in memory for the session (listings themselves are not
  persisted yet — that's a separate roadmap item).
- Uses the modern Android Photo Picker (no storage permission needed).
