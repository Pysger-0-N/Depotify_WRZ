# Walkthrough - Vendor Addition via Card Form

Refactored the vendor management screen (`VendorScreen.kt`) to use an inline form card system consistent with the category management screen (`CategoryScreen.kt`), replacing the bottom-right Floating Action Button (FAB) and popup dialog.

## Changes

### UI & Layout

#### [VendorScreen.kt](file:///D:/SMK/XII%20RPL%20B/Project%20Warung%20ZaRa/Android/app/src/main/java/com/radityaf/depotify_wrz/ui/owner/vendor/VendorScreen.kt)

- Removed `FloatingActionButton` and `AlertDialog`.
- Added a `SectionCard` for the "Tambah Vendor" form containing:
  - `DepotifyTextField` for "Nama Vendor"
  - `DepotifyTextField` for "No. Telepon / HP"
  - `DepotifyButton` ("Tambah Vendor")
- Added a `SectionCard` for "Daftar Vendor" displaying the list of vendors or an `EmptyState` when none exist, with delete actions.

```diff
-    Scaffold(
-        modifier = modifier,
-        containerColor = DepotifyColors.Background,
-        topBar = { DepotifyTopBar("Vendor", onBack) },
-        floatingActionButton = {
-            FloatingActionButton(
-                onClick = { showDialog = true },
-                containerColor = DepotifyColors.Green,
-                contentColor = DepotifyColors.Navy
-            ) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
-        }
-    ) { padding ->
...
+    Scaffold(
+        modifier = modifier,
+        containerColor = DepotifyColors.Background,
+        topBar = { DepotifyTopBar("Vendor", onBack) }
+    ) { padding ->
+        LazyColumn(
+            modifier = Modifier.padding(padding),
+            contentPadding = PaddingValues(16.dp),
+            verticalArrangement = Arrangement.spacedBy(12.dp)
+        ) {
+            item {
+                PageHeader("Master Data", "Vendor", "Pemasok gas LPG dan air mineral.")
+            }
+
+            // form tambah vendor
+            item {
+                SectionCard(label = "Form", title = "Tambah Vendor") {
+                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
+                        DepotifyTextField(
+                            label = "Nama Vendor",
+                            value = name,
+                            onValueChange = { name = it },
+                            placeholder = "Nama vendor"
+                        )
+                        DepotifyTextField(
+                            label = "No. Telepon / HP",
+                            value = phone,
+                            onValueChange = { phone = it },
+                            placeholder = "08xxxxxxxxxx",
+                            keyboardType = KeyboardType.Phone
+                        )
+                        DepotifyButton(
+                            text = "Tambah Vendor",
+                            onClick = {
+                                viewModel.add(name, phone)
+                                name = ""
+                                phone = ""
+                            },
+                            enabled = name.isNotBlank(),
+                            modifier = Modifier.fillMaxWidth()
+                        )
+                    }
+                }
+            }
+            ...
```

## Verification Results

### Automated Tests
- Executed `gradle_build("app:assembleDebug")` successfully with exit status `Build finished successfully.`.
