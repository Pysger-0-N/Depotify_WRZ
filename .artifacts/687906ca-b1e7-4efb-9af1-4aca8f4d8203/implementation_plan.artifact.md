# Implement Vendor Addition via Form Card

Refactor the Vendor screen (`VendorScreen.kt`) to replace the Floating Action Button (FAB) and AlertDialog with an inline form card (similar to `CategoryScreen`), allowing users to add vendors directly from the page layout.

## User Review Required

> [!NOTE]
> The vendor form will now be presented as a persistent `SectionCard` labeled "Form" with "Tambah Vendor", containing input fields for Name and Phone and an action button, followed by a data card listing existing vendors.

## Open Questions

- None. The design directly mirrors `CategoryScreen` while accommodating both vendor name and phone number fields.

## Proposed Changes

### UI Components

#### [MODIFY] [VendorScreen.kt](file:///D:/SMK/XII%20RPL%20B/Project%20Warung%20ZaRa/Android/app/src/main/java/com/radityaf/depotify_wrz/ui/owner/vendor/VendorScreen.kt)
- Remove `floatingActionButton` and `showDialog` / `AlertDialog`.
- Add a Form `SectionCard` containing `DepotifyTextField` for Name and Phone, and a `DepotifyButton` to add the vendor.
- Add a Data `SectionCard` containing the list of vendors (`state.items`) or `EmptyState` if empty, with delete action buttons.

## Verification Plan

### Automated Tests
- Build the project using `gradle_build("app:assembleDebug")` to ensure compilation and Hilt dependency injection succeed.

### Manual Verification
- Review the layout via code or run the app to verify the vendor addition form card is visible and functional.
