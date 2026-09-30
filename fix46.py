# Petpulse fix46 - final two build fixes
# (fix45 already fixed the commerce + AdminScreen issues on your phone)
# Run inside ~/Petpulse :   python fix46.py

report = []

# ---------- FIX 1: PetModels.kt - comma after status (line 57 error) ----------
p = 'app/src/main/java/com/example/data/model/PetModels.kt'
try:
    s = open(p, encoding='utf-8').read()
    broken = '    val status: String = "" // "Completed" or "Upcoming"\n    val veterinarian'
    fixed  = '    val status: String = "", // "Completed" or "Upcoming"\n    val veterinarian'
    if broken in s:
        s = s.replace(broken, fixed, 1)
        open(p, 'w', encoding='utf-8').write(s)
        report.append(('FIX 1  PetModels comma', 'FIXED'))
    elif fixed in s:
        report.append(('FIX 1  PetModels comma', 'already OK'))
    else:
        report.append(('FIX 1  PetModels comma', '!!! NOT FOUND - lines 50-60 are:'))
        for i, l in enumerate(s.split('\n')[49:60], start=50):
            print(str(i) + ': ' + l)
except Exception as e:
    report.append(('FIX 1  PetModels comma', 'ERROR: ' + str(e)))

# ---------- FIX 2: PartnersServicesScreen.kt - missing LocalContext import ----------
p = 'app/src/main/java/com/example/ui/screens/PartnersServicesScreen.kt'
try:
    s = open(p, encoding='utf-8').read()
    imp = 'import androidx.compose.ui.platform.LocalContext'
    if imp in s:
        report.append(('FIX 2  LocalContext import', 'already OK'))
    else:
        anchor = 'import androidx.compose.ui.platform.testTag'
        if anchor not in s:
            anchor = 'import androidx.compose.ui.layout.ContentScale'
        if anchor in s:
            s = s.replace(anchor, imp + '\n' + anchor, 1)
            open(p, 'w', encoding='utf-8').write(s)
            report.append(('FIX 2  LocalContext import', 'FIXED'))
        else:
            report.append(('FIX 2  LocalContext import', '!!! no anchor found - send me the first 60 lines'))
except Exception as e:
    report.append(('FIX 2  LocalContext import', 'ERROR: ' + str(e)))

# ---------- verify brace balance ----------
def balance(path):
    s = open(path, encoding='utf-8').read()
    depth = 0; in_bc = False
    for l in s.split('\n'):
        res = ''; j = 0; in_str = False
        while j < len(l):
            if in_bc:
                if l[j:j+2] == '*/': in_bc = False; j += 2; continue
                j += 1; continue
            if in_str:
                if l[j] == chr(92): j += 2; continue
                if l[j] == '"': in_str = False
                j += 1; continue
            if l[j:j+2] == '//': break
            if l[j:j+2] == '/*': in_bc = True; j += 2; continue
            if l[j] == '"': in_str = True; j += 1; continue
            res += l[j]; j += 1
        depth += res.count('{') - res.count('}')
    return depth

print()
print('--- brace-balance verification ---')
ok = True
for f in ['app/src/main/java/com/example/data/model/PetModels.kt',
          'app/src/main/java/com/example/ui/screens/PartnersServicesScreen.kt']:
    d = balance(f)
    print(f.split('/')[-1] + ': ' + ('OK' if d == 0 else '!!! UNBALANCED (' + str(d) + ')'))
    if d != 0: ok = False

print()
print('================ SUMMARY ================')
for name, st in report:
    print(name.ljust(34) + ': ' + st)
bad = any(('!!!' in st) or ('ERROR' in st) for _, st in report)
print('=========================================')
if bad or not ok:
    print('SOME FIXES FAILED - screenshot this whole output and send it to me!')
else:
    print('ALL FIXED! Now run:')
    print('  git add -A')
    print('  git commit -m "fix: status comma and missing LocalContext import"')
    print('  git push')
