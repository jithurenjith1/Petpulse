package com.example.data.repository

import com.example.data.local.PetDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class PetRepository(private val petDao: PetDao) {

    val defaultPet: Flow<UserPet?> = petDao.getDefaultPet()
    val allUserPets: Flow<List<UserPet>> = petDao.getAllUserPets()
    val lostPetAlerts: Flow<List<LostPetAlert>> = petDao.getAllLostPetAlerts()
    val petListings: Flow<List<PetListing>> = petDao.getAllPetListings()

    fun getVaccinations(petId: Long): Flow<List<VaccinationRecord>> =
        petDao.getVaccinationsForPet(petId)

    fun getMedicalReports(petId: Long): Flow<List<MedicalReport>> =
        petDao.getMedicalReportsForPet(petId)

    suspend fun savePet(pet: UserPet) = petDao.insertOrUpdatePet(pet)

    suspend fun addVaccination(record: VaccinationRecord) = petDao.insertVaccination(record)

    suspend fun updateVaccinationStatus(id: Long, newStatus: String) =
        petDao.updateVaccinationStatus(id, newStatus)

    suspend fun addMedicalReport(report: MedicalReport) = petDao.insertMedicalReport(report)

    suspend fun reportLostPet(alert: LostPetAlert) = petDao.insertLostPetAlert(alert)

    suspend fun addPetListing(listing: PetListing) = petDao.insertPetListing(listing)

    // Species Catalog Data
    fun getSpeciesCategories(): List<SpeciesCategory> = listOf(
        SpeciesCategory("dogs", "Dogs", "🐕", "Canine companions, breeds, nutrition & training"),
        SpeciesCategory("cats", "Cats", "🐈", "Feline care, scratching posts, wet food & wellness"),
        SpeciesCategory("birds", "Birds", "🦜", "Parrots, finches, seeds, flight cages & singing"),
        SpeciesCategory("fishes", "Fishes", "🐠", "Aquarium setup, flakes, filtration & aquatic health"),
        SpeciesCategory("rabbit", "Rabbit", "🐇", "Hay diet, chew toys, hutch hygiene & grooming"),
        SpeciesCategory("hamster", "Hamster", "🐹", "Exercise wheels, grain mixes, bedding & burrowing"),
        SpeciesCategory("exotic", "Exotic Species", "🦎", "Reptiles, sugar gliders, terrariums & thermal care")
    )

    fun getFoodForSpecies(speciesId: String): List<FoodItem> = when (speciesId) {
        "dogs" -> listOf(
            FoodItem("Premium High-Protein Dry Kibble", "Dry Food", "Real salmon & sweet potato formula with glucosamine for joint health.", "2 cups / day", "$45.99 / 15 lb"),
            FoodItem("Slow-Cooked Turkey & Veggie Wet Food", "Wet Food", "Grain-free savory wet stew packed with hydration and vitamins.", "1 can / day", "$28.50 / 12 pack"),
            FoodItem("Crunchy Dental Peanut Butter Treats", "Treats", "Enzymatic teeth-cleaning chews with organic peanut butter.", "2 treats / day", "$12.99 / bag"),
            FoodItem("Organic Freeze-Dried Beef Liver Bites", "Treats", "Single-ingredient high-value training treat for command focus.", "3-4 pieces / session", "$16.50 / jar")
        )
        "cats" -> listOf(
            FoodItem("Ocean Whitefish & Tuna Grain-Free Kibble", "Dry Food", "Rich in Omega-3 fatty acids for a shiny coat and urinary tract support.", "0.5 cup / day", "$38.00 / 10 lb"),
            FoodItem("Tender Chicken in Savory Broth Paté", "Wet Food", "High moisture wet food to prevent kidney dehydration.", "2 pouches / day", "$24.00 / 24 pack"),
            FoodItem("Catnip Infused Crispy Dental Bites", "Treats", "Irresistible crunchy bites with real catnip and tartar control.", "5-6 bites / day", "$8.99 / pack"),
            FoodItem("Creamy Lickable Purée Squeeze Tubes", "Treats", "Interactive hand-feeding treat for bonding and medication masking.", "1 tube / day", "$11.50 / 8 pack")
        )
        "birds" -> listOf(
            FoodItem("Fortified Seed & Nut Medley", "Dry Food", "Pellet-seed blend with sunflower seeds, millet, and added calcium.", "2 tbsp / day", "$18.99 / 5 lb"),
            FoodItem("Fresh Sprout & Veggie Mash Purée", "Wet Food", "Fresh organic broccoli, corn, and sprouted grains mash.", "1 tbsp / morning", "$14.00 / pack"),
            FoodItem("Honey & Fruit Seed Bell Treat", "Treats", "Hanging bell treat that encourages natural foraging instincts.", "1 bell / week", "$7.50 / 2 pack")
        )
        "fishes" -> listOf(
            FoodItem("Tropical Micro-Flakes Color Enhancer", "Dry Food", "Spirulina and krill flakes that bring out vibrant scales.", "Pinch 2x / day", "$9.99 / container"),
            FoodItem("Freeze-Dried Bloodworms Gel Feed", "Wet Food", "High-protein aquatic delicacy suitable for bettas, tetras & cichlids.", "Small portion 3x / week", "$13.50 / pack"),
            FoodItem("Bottom Feeder Sinking Algae Wafers", "Treats", "Nutrient-dense discs for plecos, corydoras, and shrimp.", "1 wafer / evening", "$8.20 / pack")
        )
        "rabbit" -> listOf(
            FoodItem("First-Cut Timothy Hay Premium Bale", "Dry Food", "Essential long-strand fiber promoting gut motility and dental wear.", "Unlimited daily", "$22.00 / 9 lb"),
            FoodItem("Herbaceous Botanical Wet Greens Bowl", "Wet Food", "Fresh cilantro, romaine lettuce, and dandelion leafy greens.", "1 cup / day", "$9.50 / fresh pack"),
            FoodItem("Dried Meadow Flower & Apple Crisp Rings", "Treats", "Natural dried apple rings dusted with marigold petals.", "1 ring / alternate days", "$6.99 / bag")
        )
        "hamster" -> listOf(
            FoodItem("Gourmet Whole-Grain Muesli & Seed Bowl", "Dry Food", "Barley, pumpkin seeds, oats, and mealworms for omnivorous foraging.", "1 tbsp / day", "$11.99 / 2 lb"),
            FoodItem("Mashed Carrot & Pea Wet Porridge", "Wet Food", "Soft vitamin-enriched vegetable purée for gentle digestion.", "Half tsp / 2 days", "$5.50 / jar"),
            FoodItem("Crunchy Yogurt Drops & Nut Clusters", "Treats", "Delicious sweet berry and probiotic yogurt drop treats.", "1 drop / day", "$4.99 / pack")
        )
        else -> listOf(
            FoodItem("Omnivore Reptile & Gecko Powder Feed", "Dry Food", "Complete calcium-fortified meal replacement formula.", "1 scoop / day", "$19.99 / jar"),
            FoodItem("Canned Crickets in Vitamin Nectar Jelly", "Wet Food", "Pre-cooked safe feeder insects packed with digestible protein.", "2-3 crickets / meal", "$15.00 / 3 pack"),
            FoodItem("Papaya & Mango Fruit Purée Jelly Pots", "Treats", "Sugar-glider & exotic pet natural fruit treat cups.", "1 pot / 3 days", "$10.50 / 6 pack")
        )
    }

    fun getAccessoriesForSpecies(speciesId: String): List<AccessoryItem> = when (speciesId) {
        "dogs" -> listOf(
            FoodItem("", "", "", "", "") // placeholder to distinguish
        ).let {
            listOf(
                AccessoryItem("Weatherproof Blue Raincoat & Hoodie", "Clothing", "Reflective safety stripes with waterproof breathable fabric.", "$24.99", "Ripstop Nylon"),
                AccessoryItem("Indestructible Natural Rubber Chew Ball", "Toys", "Textured teeth-cleaning ball with treat dispensing slot.", "$14.50", "BPA-Free Rubber"),
                AccessoryItem("Smart LED Safety Collar & Leash Set", "Wearables", "USB rechargeable glow collar with 3 flash modes.", "$19.99", "Braided Nylon"),
                AccessoryItem("Orthopedic Memory Foam Pet Bed", "Other", "Soothing joint support bed with removable washable cover.", "$58.00", "Memory Foam & Sherpa")
            )
        }
        "cats" -> listOf(
            AccessoryItem("Warm Knitted Fleece Sweater", "Clothing", "Cozy winter pullover with stretchy ribbed collar.", "$16.99", "Soft Acrylic Knit"),
            AccessoryItem("3-Tier Sisal Cat Scratching Tree & Hammock", "Toys", "Sturdy tower with dangling teaser balls and plush perch.", "$49.99", "Natural Sisal Rope"),
            AccessoryItem("Breakaway Velvet Collar with Blue Bell", "Wearables", "Quick-release safety buckle designed specifically for cats.", "$9.99", "Plush Velvet"),
            AccessoryItem("Ultra-Quiet Automatic Water Fountain Filter", "Other", "2.5L circulating filtered waterer to promote kidney health.", "$29.50", "Stainless Steel")
        )
        "birds" -> listOf(
            AccessoryItem("Natural Wooden Ladder & Spiral Rope Perch", "Toys", "Cotton spiral rope swing with colorful chewable wooden blocks.", "$15.99", "Non-toxic Pine & Cotton"),
            AccessoryItem("Breathable Flight Harness with Leash", "Wearables", "Lightweight ergonomic flight suit for safe outdoor adventures.", "$18.50", "Elastic Fabric"),
            AccessoryItem("Bird Bath Shower with Mirror Bracket", "Other", "Clip-on clear bath tub for splash and feather conditioning.", "$12.00", "BPA-free Acrylic")
        )
        "fishes" -> listOf(
            AccessoryItem("Glowing Coral Reef Aquarium Ornament", "Toys", "Fluorescent non-toxic cave ornament for fish exploration.", "$14.99", "Aquatic Safe Resin"),
            AccessoryItem("Submersible RGB Multi-Color Aquarium Light", "Wearables", "Programmable sunrise/sunset LED bar with remote control.", "$26.00", "IP68 Waterproof Aluminum"),
            AccessoryItem("Magnetic Glass Cleaner with Algae Scraper", "Other", "Dual-sided floating magnetic glass wiper for effortless crystal views.", "$11.50", "High-strength Magnet")
        )
        "rabbit" -> listOf(
            AccessoryItem("Apple Wood Chew Sticks & Loofah Carrot", "Toys", "Natural dental grinding sticks that protect furniture.", "$8.99", "Natural Apple Orchard Wood"),
            AccessoryItem("Padded Escape-Proof Mesh Harness", "Wearables", "Comfortable soft vest harness for backyard hops.", "$14.50", "Breathable Mesh"),
            AccessoryItem("Spacious Corner Litter Box with Hay Feeder", "Other", "2-in-1 hay rack and deep grid litter pan.", "$22.00", "Sturdy Polymer")
        )
        "hamster" -> listOf(
            AccessoryItem("Silent Spinner Exercise Wheel (8.5 inch)", "Toys", "Dual ball-bearing ultra-quiet running wheel.", "$16.99", "Smooth ABS Plastic"),
            AccessoryItem("Natural Wooden Maze Castle with Burrow Tunnel", "Other", "Multi-chamber hideout that mimics natural underground burrows.", "$21.50", "Unfinished Birch Wood"),
            AccessoryItem("Ceramic Cooling Hideout House", "Other", "Heat-relief ceramic igloo for hot summer days.", "$12.00", "Glazed Ceramic")
        )
        else -> listOf(
            AccessoryItem("Digital Humidity & Thermal Hygrometer Sensor", "Wearables", "High precision dual-probe sensor for optimal vivarium climate.", "$17.99", "Digital Probe"),
            AccessoryItem("Bendable Jungle Vine & Reptile Hammock", "Toys", "Flexible textured climbing vine with heavy-duty suction cups.", "$15.50", "Natural Texture Poly"),
            AccessoryItem("Ceramic Infrared Heat Emitter Lamp (75W)", "Other", "24-hour heat source that produces zero light disruption.", "$19.00", "Ceramic Core")
        )
    }

    fun getHealthCareForSpecies(speciesId: String): List<HealthCareItem> = listOf(
        HealthCareItem("Professional Coat De-Shedding & Bath", "Grooming", "Full deep cleansing bath, blow dry, nail clipping, and ear cleaning.", "Every 4-6 Weeks", "$35 - $65"),
        HealthCareItem("Core Immunity Vaccinations", "Vaccination", "Rabies, viral booster shots, and preventative immunization schedule.", "Annual / Multi-Year", "$25 - $50"),
        HealthCareItem("Routine Wellness & Dental Checkup", "Checkups", "Comprehensive physical examination, vitals, coat, and tartar inspection.", "Bi-Annual", "$40 - $75"),
        HealthCareItem("Tick, Flea & Parasite Prevention", "Treatments", "Topical or chewable broad-spectrum protection against parasites.", "Monthly Routine", "$15 - $30")
    )

    fun getTrainingGuidesForSpecies(speciesId: String): List<TrainingGuide> = when (speciesId) {
        "dogs" -> listOf(
            TrainingGuide(
                title = "Basic Dog Obedience (Sit, Stay, Paw & Come)",
                level = "Basic",
                steps = listOf(
                    "Hold a high-value treat close to the dog's nose.",
                    "Move your hand up, allowing the head to follow the treat and causing the bottom to lower.",
                    "Once in the sit position, say 'Sit', mark with 'Yes!' and reward immediately.",
                    "Gradually add 3 seconds of pause before delivering the treat to build 'Stay'.",
                    "Practice in 5-minute intervals twice daily with praise."
                ),
                tips = "Never scold for mistakes; Indie dogs learn rapidly through upbeat positive reinforcement.",
                recommendedAge = "From 8 Weeks Onwards"
            ),
            TrainingGuide(
                title = "Advanced Agility & Off-Leash Recall",
                level = "Advanced",
                steps = listOf(
                    "Establish a reliable whistle or unique verbal marker.",
                    "Use a 30-foot long line in a secure open park.",
                    "Call the recall marker while running backwards; reward generously when the dog reaches you.",
                    "Introduce hurdle hops and weave poles with target touch sticks.",
                    "Gradually phase out the long line in enclosed dog parks."
                ),
                tips = "Maintain high-value rewards (freeze-dried liver) exclusively for recall mastery.",
                recommendedAge = "6 Months & Above"
            )
        )
        "cats" -> listOf(
            TrainingGuide(
                title = "Cat Litter Box & Scratching Post Habituation",
                level = "Basic",
                steps = listOf(
                    "Place the litter box in a quiet, low-traffic area.",
                    "Gently place your cat in the box after meals and naps.",
                    "Rub catnip on sisal scratching posts placed near sleeping spots.",
                    "Reward scratching posts with lickable treats."
                ),
                tips = "Keep litter depth around 2 inches and scoop daily.",
                recommendedAge = "All Ages"
            ),
            TrainingGuide(
                title = "Cat High-Five & Carrier Comfort Training",
                level = "Advanced",
                steps = listOf(
                    "Leave the travel carrier open at home with cozy blankets and treats inside.",
                    "Hold a treat in a closed fist; when paw touches hand, say 'High Five' and open palm.",
                    "Pair carrier entry with gentle clicker training."
                ),
                tips = "Cats respond best to clicker marker training during pre-dinner play sessions.",
                recommendedAge = "4 Months+"
            )
        )
        else -> listOf(
            TrainingGuide(
                title = "Hand-Taming & Trust Building Routine",
                level = "Basic",
                steps = listOf(
                    "Place your hand calmly resting outside the cage/enclosure for 5 minutes.",
                    "Offer tasty millet sprays or favorite seeds from fingertips.",
                    "Allow pet to step onto palm at their own comfortable pace.",
                    "Speak in soft, reassuring tones."
                ),
                tips = "Patience is key; avoid sudden movements or grabbing.",
                recommendedAge = "Any Stage"
            ),
            TrainingGuide(
                title = "Target Stick Guidance & Foraging Tricks",
                level = "Advanced",
                steps = listOf(
                    "Present a rounded target stick near the pet.",
                    "When pet touches tip with beak/nose, click and deliver a favorite seed treat.",
                    "Lead pet through mini obstacle tunnels and foraging puzzles."
                ),
                tips = "Provides crucial mental stimulation and prevents boredom.",
                recommendedAge = "Young Adults"
            )
        )
    }

    // Partner Businesses
    fun getFeaturedGroomingCenters(): List<GroomingCenter> = listOf(
        GroomingCenter(
            name = "Jane's Grooming Studio",
            tagLine = "Luxury Spa & Styling Center for Dogs, Cats & Small Pets",
            address = "742 Blue Ridge Ave, Metro Pet District",
            distance = "1.2 km away",
            rating = 4.9,
            reviewCount = 384,
            packages = listOf("Full Hydro-Bath & Blowout", "Breed-Specific Styling & Trim", "Pawdicure & Balm Treatment", "Organic Herbal Flea Rinse"),
            startingPrice = "$35.00",
            phone = "+1 (555) 924-JANE",
            isFeaturedPartner = true
        ),
        GroomingCenter(
            name = "Paws & Whiskers Mobile Spa",
            tagLine = "Doorstep Luxury Grooming Van with Warm Water Jet",
            address = "On-Demand Doorstep Service (Covers 10 km Radius)",
            distance = "Mobile Unit",
            rating = 4.8,
            reviewCount = 210,
            packages = listOf("Doorstep Full Spa", "De-matting & Ear Cleaning", "Teeth Brushing & Breath Freshener"),
            startingPrice = "$49.00",
            phone = "+1 (555) 432-PAWS",
            isFeaturedPartner = true
        )
    )

    fun getFoodSubscriptionPlans(): List<FoodSubscription> = listOf(
        FoodSubscription(
            title = "Royal Canine & Indie Care Combo",
            planType = "Monthly",
            comboContents = "15kg High-Protein Kibble + 12 Cans Gourmet Wet Gravy + 2 Dental Chews Packs + 1 Squeaky Toy",
            brandsIncluded = "Royal Canin, Farmina N&D, Pedigree Pro",
            monthlyEstimate = "$59.99 / month",
            savingsTag = "Save 25% vs Retail"
        ),
        FoodSubscription(
            title = "Annual VIP Pet Nutrition & Toy Crate",
            planType = "Yearly",
            comboContents = "Monthly auto-shipped custom food box + Free Birthday Gift Crate + Free Grooming Studio Vouchers",
            brandsIncluded = "Acana, Orijen, Purina Pro, Whiskas Gourmet",
            monthlyEstimate = "$590.00 / year ($49/mo)",
            savingsTag = "Best Value • Free GPS Tag"
        ),
        FoodSubscription(
            title = "Feline Gourmet & Kitty Treat Box",
            planType = "Monthly",
            comboContents = "6kg Fish & Poultry Dry Food + 24 Pouches Broth + Lickable Treats + Feather Wand Toy",
            brandsIncluded = "Taste of the Wild, Sheba, Felix",
            monthlyEstimate = "$42.50 / month",
            savingsTag = "Save 20% Monthly"
        )
    )

    fun getBoardingSitters(): List<BoardingSitter> = listOf(
        BoardingSitter(
            name = "Dora Henderson",
            sitterType = "Full Day (24hr)",
            tagline = "Certified 24-Hour In-Home Pet Sitter with Fenced Yard & 24/7 Web-Cam Access",
            experience = "6+ Years Experience • 450+ Happy Dogs Hosted",
            rating = 4.98,
            priceEstimate = "$45 / 24 Hours",
            features = listOf("24/7 Live Stream Camera for Owners", "3 Daily Walks & Social Playtime", "Administers Oral Medications", "Daily Photo/Video Updates"),
            verified = true
        ),
        BoardingSitter(
            name = "Sunny Meadows Daycare",
            sitterType = "Per Day Care",
            tagline = "Spacious Outdoor Playgrounds & Socialization Agility Yard (8 AM - 7 PM)",
            experience = "Licensed Facility • Vet on Call",
            rating = 4.85,
            priceEstimate = "$28 / Day",
            features = listOf("Agility Grass Park", "Puppy Splash Pool", "Separated Small & Large Dog Zones", "Nap Cabin with AC"),
            verified = true
        ),
        BoardingSitter(
            name = "Cozy Nook Overnight Haven",
            sitterType = "Pet Night Care",
            tagline = "Quiet, sound-proof climate-controlled suites for anxious & calm pets (7 PM - 9 AM)",
            experience = "Veterinary Nurse on Duty",
            rating = 4.9,
            priceEstimate = "$32 / Night",
            features = listOf("Calming Lavender Aromatherapy", "Individual Orthopedic Mattresses", "Evening Bedtime Cuddle Routine"),
            verified = true
        ),
        BoardingSitter(
            name = "Timely Paws Doorstep Feeding",
            sitterType = "Feed on Time Only",
            tagline = "Scheduled Drop-In Doorstep Visits strictly according to your pet's dietary schedule",
            experience = "Insured & Background Verified Staff",
            rating = 4.92,
            priceEstimate = "$15 / Visit",
            features = listOf("Exact Time Feeding & Fresh Water", "Litter Box / Yard Clean", "5-Min Quick Play & Snuggle", "GPS Check-in Proof"),
            verified = true
        )
    )

    fun getPetNews(): PetNewsItem = PetNewsItem(
        title = "Jane's Heroic Indie Dog Recovery Sparks Neighborhood Pet Safety Network",
        source = "Global Pet Herald & Wildlife Journal",
        timeAgo = "Today • 4 min read",
        summary = "An indie dog named Jane rescued from city streets helped inspire the smart 5km community alert system, uniting thousands of pet parents for lost pet recovery.",
        fullContent = "In a heartwarming turn of events, Jane, a resilient and affectionate indie canine, has become the beloved mascot for community-driven pet protection. Her playful spirit and quick mastery of commands proved that Indies and rescued animals possess remarkable loyalty and intelligence. Today, with over 15,000 active guardians on the Jane & Pals 5km radius alert network, pet owners can instantly coordinate with nearby neighbors, grooming partners, and verified sitters to ensure every furry family member stays safe, well-fed, and celebrated."
    )

    fun getUpcomingEvents(): List<PetEventItem> = listOf(
        PetEventItem(
            title = "Annual Metro Canine Agility & Obedience Cup 2026",
            category = "Competition",
            date = "Oct 12, 2026 • 9:00 AM",
            location = "Riverfront Stadium & Exhibition Grounds",
            entryStatus = "Open for Registrations",
            prizePool = "$2,500 Trophy & Pet Care Hampers"
        ),
        PetEventItem(
            title = "Mega Pet Adoption & Rescue Fair",
            category = "Adoption Drive",
            date = "Nov 05, 2026 • 10:30 AM",
            location = "Central Botanical Park Lawn",
            entryStatus = "Free Entry for All Families",
            prizePool = "Free Starter Care Kits for Adopted Pets"
        ),
        PetEventItem(
            title = "Fancy Felines & Birds Beauty Pageant",
            category = "Showcase",
            date = "Nov 22, 2026 • 2:00 PM",
            location = "Civic Auditorium Hall B",
            entryStatus = "Limited Slots (50 Entries)",
            prizePool = "$1,000 + Golden Ribbon"
        )
    )
}
