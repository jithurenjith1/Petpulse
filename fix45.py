import re

# ============================================================
# Petpulse fix45 - fixes the 3 compile errors from runs 103-105
# Run inside ~/Petpulse :   python fix45.py
# ============================================================

R = 'app/src/main/java/com/example/'
report = []

# ---------- FIX 1: PetModels.kt - missing comma after status ----------
p = R + 'data/model/PetModels.kt'
try:
    s = open(p, encoding='utf-8').read()
    broken = '    val status: String = ""\n    val veterinarian'
    fixed  = '    val status: String = "",\n    val veterinarian'
    if broken in s:
        s = s.replace(broken, fixed, 1)
        open(p, 'w', encoding='utf-8').write(s)
        report.append(('FIX 1  PetModels.kt comma', 'FIXED'))
    elif fixed in s:
        report.append(('FIX 1  PetModels.kt comma', 'already OK'))
    else:
        report.append(('FIX 1  PetModels.kt comma', '!!! PATTERN NOT FOUND'))
except Exception as e:
    report.append(('FIX 1  PetModels.kt comma', 'ERROR: ' + str(e)))

# ---------- FIX 2: FirestoreCommerceRepository.kt - return in expression body ----------
p = R + 'data/repository/FirestoreCommerceRepository.kt'
try:
    s = open(p, encoding='utf-8').read()
    changed = 0

    old_sub = '''    suspend fun submitSupportTicket(t: SupportTicket): Result<Unit> = try {
        auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        db.collection("support_tickets").document().set(
            mapOf(
                "category" to t.category,
                "subject" to t.subject,
                "details" to t.details,
                "contact" to t.contact,
                "createdAt" to System.currentTimeMillis(),
                "status" to "New"
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }'''
    new_sub = '''    suspend fun submitSupportTicket(t: SupportTicket): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            db.collection("support_tickets").document().set(
                mapOf(
                    "category" to t.category,
                    "subject" to t.subject,
                    "details" to t.details,
                    "contact" to t.contact,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "New"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }'''
    if old_sub in s:
        s = s.replace(old_sub, new_sub, 1); changed += 1
    elif new_sub in s:
        pass  # already ok
    else:
        report.append(('FIX 2a submitSupportTicket', '!!! PATTERN NOT FOUND'))

    old_res = '''    suspend fun submitRescueReport(r: RescueReport): Result<Unit> = try {
        auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        db.collection("rescue_reports").document().set(
            mapOf(
                "animalType" to r.animalType,
                "description" to r.description,
                "location" to r.location,
                "contact" to r.contact,
                "createdAt" to System.currentTimeMillis(),
                "status" to "New"
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }'''
    new_res = '''    suspend fun submitRescueReport(r: RescueReport): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(IllegalStateException("NOT_SIGNED_IN"))
        return try {
            db.collection("rescue_reports").document().set(
                mapOf(
                    "animalType" to r.animalType,
                    "description" to r.description,
                    "location" to r.location,
                    "contact" to r.contact,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "New"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }'''
    if old_res in s:
        s = s.replace(old_res, new_res, 1); changed += 1
    elif new_res in s:
        pass  # already ok
    else:
        report.append(('FIX 2b submitRescueReport', '!!! PATTERN NOT FOUND'))

    if changed > 0 or 'FIX 2a' not in str(report):
        open(p, 'w', encoding='utf-8').write(s)
    report.append(('FIX 2  commerce submit fns', 'FIXED (' + str(changed) + ' rewritten)' if changed else 'already OK'))
except Exception as e:
    report.append(('FIX 2  commerce submit fns', 'ERROR: ' + str(e)))

# ---------- FIX 3: AdminScreen.kt - missing imports ----------
p = R + 'ui/screens/AdminScreen.kt'
try:
    s = open(p, encoding='utf-8').read()
    if 'import com.petpulse.app.data.model.SupportTicket' in s:
        report.append(('FIX 3  AdminScreen imports', 'already OK'))
    else:
        m = re.search(r'^import com\.petpulse\.app\.data\.model\.[A-Za-z0-9_]+$', s, re.M)
        assert m, 'no model import line found to anchor on'
        ins = 'import com.petpulse.app.data.model.RescueReport\nimport com.petpulse.app.data.model.SupportTicket\n'
        s = s[:m.end()] + '\n' + ins.rstrip('\n') + s[m.end():]
        # clean: avoid accidental double blank handling
        s = s.replace(m.group(0) + '\nimport com.petpulse.app.data.model.RescueReport', m.group(0) + '\nimport com.petpulse.app.data.model.RescueReport', 1)
        open(p, 'w', encoding='utf-8').write(s)
        report.append(('FIX 3  AdminScreen imports', 'FIXED'))
except Exception as e:
    report.append(('FIX 3  AdminScreen imports', 'ERROR: ' + str(e)))

# ---------- FIX 4: PartnersServicesScreen.kt - localContext typo ----------
p = R + 'ui/screens/PartnersServicesScreen.kt'
try:
    s = open(p, encoding='utf-8').read()
    lines = s.split('\n')
    hits = [(i + 1, l.strip()) for i, l in enumerate(lines) if 'ocalContext' in l]
    if hits:
        print('--- localContext lines BEFORE fix ---')
        for n, l in hits:
            print(str(n) + ': ' + l)
        # known typo patterns
        s = s.replace('val marketCtx = localContext.current', 'val marketCtx = LocalContext.current')
        s = s.replace('val ctx = localContext.current', 'val ctx = LocalContext.current')
        s = s.replace('localContext.startActivity', 'marketCtx.startActivity')
        # any remaining bare lowercase uses (not a declaration, not property access)
        s = re.sub(r'(?<!val )(?<!\w)localContext(?!\w)', 'LocalContext', s)
        open(p, 'w', encoding='utf-8').write(s)
        lines2 = s.split('\n')
        left = [(i + 1, l.strip()) for i, l in enumerate(lines2) if re.search(r'(?<!val )(?<!\w)localContext(?!\w)', l)]
        report.append(('FIX 4  Partners localContext', 'FIXED (' + str(len(hits)) + ' lines)' + ('' if not left else ' !!! STILL LEFT: ' + str(left))))
    else:
        print('--- no localContext found; lines 870-878 ---')
        for i in range(869, min(878, len(lines))):
            print(str(i + 1) + ': ' + lines[i])
        report.append(('FIX 4  Partners localContext', 'none found - check lines printed above'))
except Exception as e:
    report.append(('FIX 4  Partners localContext', 'ERROR: ' + str(e)))

# ---------- VERIFY: brace balance on all touched files ----------
print()
print('--- brace-balance verification ---')
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

ok = True
for f in ['data/model/PetModels.kt',
          'data/repository/FirestoreCommerceRepository.kt',
          'ui/screens/AdminScreen.kt',
          'ui/screens/PartnersServicesScreen.kt']:
    d = balance(R + f)
    status = 'OK' if d == 0 else '!!! UNBALANCED (' + str(d) + ')'
    if d != 0: ok = False
    print(f.split('/')[-1] + ': ' + status)

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
    print('  git commit -m "fix: comma, imports and context typos from help-rescue build"')
    print('  git push')
