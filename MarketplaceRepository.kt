package com.example.data.repository

import com.example.data.model.*

class MarketplaceRepository {

    fun getKeralaCities(): List<KeralaCity> = listOf(
        KeralaCity("all", "All Kerala", "Kerala Statewide", "Statewide Express Hub"),
        KeralaCity("kochi", "Kochi", "Ernakulam", "Panampilly Nagar & Kakkanad Hub"),
        KeralaCity("trivandrum", "Trivandrum", "Thiruvananthapuram", "Kowdiar & Kazhakoottam Hub"),
        KeralaCity("kozhikode", "Kozhikode", "Calicut", "Mavoor Road & Beach Road Hub"),
        KeralaCity("thrissur", "Thrissur", "Cultural Capital", "Swaraj Round & Ollur Hub")
    )

    fun getMarketPets(): List<MarketPet> = listOf(
        MarketPet(
            id = "pet_1",
            name = "Simba",
            species = "Dog",
            breed = "Siberian Husky (Champion Bloodline)",
            age = "3 Months",
            gender = "Male",
            city = "Kochi",
            isImportedExotic = true,
            importCountry = "Imported Bloodline (Russia/EU)",
            listingType = "Sale",
            priceInr = 38000.0,
            originalPriceInr = 45000.0,
            isVaccinated = true,
            isMicrochipped = true,
            certificationDetails = "KCI Certified + Import Quarantine Clearance",
            sellerName = "Cochin Royal K9 Kennel (Verified)",
            sellerPhone = "+91 98470 23114",
            description = "Stunning blue-eyed Siberian Husky puppy. Double coat, high energy, microchipped, dewormed 3x with full veterinary passport."
        ),
        MarketPet(
            id = "pet_2",
            name = "Maya",
            species = "Dog",
            breed = "Kerala Indie (Desi Pup)",
            age = "2 Months",
            gender = "Female",
            city = "Trivandrum",
            isImportedExotic = false,
            listingType = "Adoption",
            priceInr = 0.0,
            isVaccinated = true,
            isMicrochipped = false,
            certificationDetails = "Rescue Certificate & Free 1st Year Vax",
            sellerName = "Trivandrum Animal Rescue Trust",
            sellerPhone = "+91 94471 55210",
            description = "Extremely affectionate indie pup rescued near Kowdiar. Fully dewormed, healthy, playful with children."
        ),
        MarketPet(
            id = "pet_3",
            name = "Rio",
            species = "Bird",
            breed = "Blue & Gold Macaw",
            age = "6 Months (Hand-fed)",
            gender = "Male",
            city = "Kozhikode",
            isImportedExotic = true,
            importCountry = "Imported / Exotic CITES Reg",
            listingType = "Sale",
            priceInr = 125000.0,
            originalPriceInr = 140000.0,
            isVaccinated = true,
            isMicrochipped = true,
            certificationDetails = "PARIVESH CITES Registered + DNA Sexed",
            sellerName = "Malabar Exotic Aviary (Govt Registered)",
            sellerPhone = "+91 98460 77890",
            description = "Hand-tamed, step-up trained Blue & Gold Macaw. Extremely vocal, affectionate, comes with closed leg ring and DNA certificate."
        ),
        MarketPet(
            id = "pet_4",
            name = "Cleo",
            species = "Cat",
            breed = "Persian Doll Face (Triple Coat)",
            age = "4 Months",
            gender = "Female",
            city = "Thrissur",
            isImportedExotic = true,
            importCountry = "Exotic Purebred",
            listingType = "Sale",
            priceInr = 18500.0,
            originalPriceInr = 22000.0,
            isVaccinated = true,
            isMicrochipped = true,
            certificationDetails = "TICA Lineage Verified",
            sellerName = "Thrissur Elite Felines",
            sellerPhone = "+91 94462 88411",
            description = "Fluffy pure white Persian kitten with copper eyes. Litter box trained, eating Royal Canin Persian kitten food."
        ),
        MarketPet(
            id = "pet_5",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = "2.5 Months",
            gender = "Male",
            city = "Kochi",
            isImportedExotic = false,
            listingType = "Sale",
            priceInr = 24000.0,
            originalPriceInr = 28000.0,
            isVaccinated = true,
            isMicrochipped = true,
            certificationDetails = "KCI Registered with Microchip",
            sellerName = "Ernakulam Pet Hub Breeders",
            sellerPhone = "+91 98473 34990",
            description = "Show quality Golden Retriever puppy with thick golden coat. Gentle temperament, parents on display."
        ),
        MarketPet(
            id = "pet_6",
            name = "Draco",
            species = "Reptile",
            breed = "Central Bearded Dragon (Citrus Morph)",
            age = "5 Months",
            gender = "Male",
            city = "Kochi",
            isImportedExotic = true,
            importCountry = "Exotic Captive Bred",
            listingType = "Sale",
            priceInr = 28000.0,
            originalPriceInr = 32000.0,
            isVaccinated = false,
            isMicrochipped = false,
            certificationDetails = "PARIVESH Exotic Portal Registered",
            sellerName = "Kerala Reptile Society & Exotics",
            sellerPhone = "+91 97450 11200",
            description = "Vibrant citrus orange morph Bearded Dragon. Feeding avidly on dubia roaches, calcium-dusted greens, very docile."
        ),
        MarketPet(
            id = "pet_7",
            name = "Sunny",
            species = "Bird",
            breed = "Sun Conure (Hand-Reared)",
            age = "3.5 Months",
            gender = "Female",
            city = "Trivandrum",
            isImportedExotic = true,
            importCountry = "Exotic Aviary Bred",
            listingType = "Sale",
            priceInr = 32000.0,
            originalPriceInr = 36000.0,
            isVaccinated = true,
            isMicrochipped = false,
            certificationDetails = "Leg Banded + DNA Sexed Certificate",
            sellerName = "Travancore Aviaries",
            sellerPhone = "+91 94470 99812",
            description = "Bright yellow & orange Sun Conure. Super tame, loves shoulder sitting, whistles tunes, fully weaned."
        ),
        MarketPet(
            id = "pet_8",
            name = "Oreo",
            species = "Rabbit",
            breed = "Netherland Dwarf Rabbit",
            age = "2 Months",
            gender = "Pair (M/F)",
            city = "Thrissur",
            isImportedExotic = true,
            importCountry = "Exotic Dwarf Breed",
            listingType = "Sale",
            priceInr = 4500.0,
            originalPriceInr = 5500.0,
            isVaccinated = true,
            isMicrochipped = false,
            certificationDetails = "Health Checked & Dewormed",
            sellerName = "Kerala Bunny Boutique",
            sellerPhone = "+91 98461 44321",
            description = "Pocket-sized true dwarf bunnies with tiny ears. Very friendly, litter trained, eating Timothy hay."
        )
    )

    fun getMarketFoods(): List<MarketProduct> = listOf(
        MarketProduct(
            id = "food_1",
            name = "Royal Canin Maxi Puppy Dry Dog Food",
            brand = "Royal Canin",
            category = "Dry Kibble",
            isMedicine = false,
            packSize = "4 kg Pack",
            priceInr = 2950.0,
            originalPriceInr = 3250.0,
            rating = 4.9,
            reviewsCount = 340,
            description = "Tailored nutrition for large breed puppies (adult weight 26-44kg) up to 15 months. Supports immune defenses and digestive balance.",
            keyBenefits = listOf("Digestive health with prebiotics", "Moderate energy for long growth period", "Immune system support")
        ),
        MarketProduct(
            id = "food_2",
            name = "Farmina N&D Pumpkin Lamb & Blueberry Grain-Free",
            brand = "Farmina",
            category = "Dry Kibble",
            isMedicine = false,
            packSize = "2.5 kg Pack",
            priceInr = 3400.0,
            originalPriceInr = 3800.0,
            rating = 4.9,
            reviewsCount = 189,
            description = "Ultra-premium Italian formulation with 96% protein of animal origin. Perfect for sensitive stomachs and shiny coats.",
            keyBenefits = listOf("Grain-Free & Low Glycemic index", "Rich in natural antioxidants", "Hypoallergenic lamb formula")
        ),
        MarketProduct(
            id = "food_3",
            name = "Pedigree Pro Expert Nutrition Active Adult",
            brand = "Pedigree Pro",
            category = "Dry Kibble",
            isMedicine = false,
            packSize = "3 kg Pack",
            priceInr = 1450.0,
            originalPriceInr = 1650.0,
            rating = 4.7,
            reviewsCount = 420,
            description = "High protein and energy density for active working and energetic family dogs. Fortified with glucosamine for joints.",
            keyBenefits = listOf("28% High Crude Protein", "Omega fatty acids for alertness", "Zinc for healthy skin")
        ),
        MarketProduct(
            id = "food_4",
            name = "Kochi Fresh Farm Minced Meat & Organs Pack",
            brand = "Kerala Farm Direct",
            category = "Fresh Meat",
            isMedicine = false,
            packSize = "1 kg Vacuum Sealed",
            priceInr = 280.0,
            originalPriceInr = 320.0,
            rating = 4.9,
            reviewsCount = 512,
            description = "100% human-grade fresh Kerala chicken, liver, and bone broth paste. Flash frozen for instant raw feeding or boiling.",
            keyBenefits = listOf("Zero preservatives", "Delivered in temperature-controlled bag", "High natural moisture & taurine")
        ),
        MarketProduct(
            id = "food_5",
            name = "Drools Focus Super Premium Adult Dog Food",
            brand = "Drools",
            category = "Dry Kibble",
            isMedicine = false,
            packSize = "4 kg Pack",
            priceInr = 1850.0,
            originalPriceInr = 2100.0,
            rating = 4.6,
            reviewsCount = 280,
            description = "Real chicken kibble with no wheat, corn, or soya. Formulated with DHA for brain functioning.",
            keyBenefits = listOf("No fillers or corn", "Glucosamine & Chondroitin", "Optimal digestion")
        ),
        MarketProduct(
            id = "food_6",
            name = "Whiskas Ocean Fish Wet Cat Food Gravy Pouches",
            brand = "Whiskas",
            category = "Wet Food",
            isMedicine = false,
            packSize = "Pack of 12 (85g each)",
            priceInr = 600.0,
            originalPriceInr = 720.0,
            rating = 4.8,
            reviewsCount = 610,
            description = "Delicious fish chunks bathed in nutritious gravy. Hydrates cats and provides balanced minerals for urinary care.",
            keyBenefits = listOf("Prevents urinary tract issues", "Shiny coat with Omega 6", "Lickable tender chunks")
        )
    )

    fun getMarketMedicines(): List<MarketProduct> = listOf(
        MarketProduct(
            id = "med_1",
            name = "Bravecto Chewable Tablet for Dogs (10-20 kg)",
            brand = "MSD Animal Health",
            category = "Tick & Flea",
            isMedicine = true,
            prescriptionRequired = false,
            packSize = "1 Chewable Tab (12-Week Protection)",
            priceInr = 1850.0,
            originalPriceInr = 2100.0,
            rating = 4.9,
            reviewsCount = 480,
            description = "The gold-standard single oral chew providing 12 full weeks of continuous protection against ticks and fleas in humid Kerala weather.",
            keyBenefits = listOf("12 Weeks single dose protection", "Tasty pork flavor chew", "Starts killing ticks in 8 hours"),
            dosageOrUsage = "1 tablet every 12 weeks for dogs 10-20kg"
        ),
        MarketProduct(
            id = "med_2",
            name = "Drontal Plus Deworming Tablets for Dogs",
            brand = "Bayer / Elanco",
            category = "Dewormer",
            isMedicine = true,
            prescriptionRequired = false,
            packSize = "Strip of 4 Tablets",
            priceInr = 380.0,
            originalPriceInr = 440.0,
            rating = 4.8,
            reviewsCount = 390,
            description = "Broad spectrum dewormer effective against tapeworms, roundworms, hookworms, and whipworms in dogs.",
            keyBenefits = listOf("Treats all major intestinal worms", "Easy to administer scored tablet", "Vet recommended safe formula"),
            dosageOrUsage = "1 tablet per 10 kg body weight every 3 months"
        ),
        MarketProduct(
            id = "med_3",
            name = "Savavet Cephavet 600 mg Antibiotic",
            brand = "Savavet",
            category = "Antibiotics",
            isMedicine = true,
            prescriptionRequired = true,
            packSize = "Strip of 10 Tablets",
            priceInr = 320.0,
            originalPriceInr = 370.0,
            rating = 4.7,
            reviewsCount = 145,
            description = "Cephalexin oral antibiotic prescribed for skin infections, deep pyoderma, wound healing, and soft tissue infections.",
            keyBenefits = listOf("Fast action against bacterial skin infections", "Trusted veterinary grade"),
            dosageOrUsage = "As prescribed by registered Kerala veterinary doctor"
        ),
        MarketProduct(
            id = "med_4",
            name = "Zipvit Multivitamin & Amino Acid Syrup",
            brand = "Virbac",
            category = "Supplements",
            isMedicine = true,
            prescriptionRequired = false,
            packSize = "200 ml Bottle",
            priceInr = 245.0,
            originalPriceInr = 290.0,
            rating = 4.8,
            reviewsCount = 310,
            description = "Essential vitamin complex with Taurine and L-Carnitine for appetite boosting, post-illness recovery, and overall vitality.",
            keyBenefits = listOf("Boosts immunity & appetite", "Glossy coat and skin health", "Pleasant palatable flavor"),
            dosageOrUsage = "5ml daily for small pets, 10ml for medium/large dogs"
        ),
        MarketProduct(
            id = "med_5",
            name = "Fiprofort Plus Spot-On for Dogs (10-20 kg)",
            brand = "Savavet",
            category = "Tick & Flea",
            isMedicine = true,
            prescriptionRequired = false,
            packSize = "1.34 ml Pipette",
            priceInr = 420.0,
            originalPriceInr = 480.0,
            rating = 4.7,
            reviewsCount = 275,
            description = "Fipronil & (S)-Methoprene topical spot-on breaking the flea life cycle (eggs, larvae, adults) and killing brown dog ticks.",
            keyBenefits = listOf("Fast spot-on neck application", "Waterproof after 24 hours", "Prevents re-infestation for 30 days"),
            dosageOrUsage = "Apply 1 pipette directly on skin at back of neck"
        ),
        MarketProduct(
            id = "med_6",
            name = "Synoquin EFA Joint Support Chewable Tablets",
            brand = "VetPlus",
            category = "Supplements",
            isMedicine = true,
            prescriptionRequired = false,
            packSize = "Pack of 30 Tablets",
            priceInr = 1650.0,
            originalPriceInr = 1900.0,
            rating = 4.9,
            reviewsCount = 160,
            description = "Contains Glucosamine, Chondroitin, and Dexahan (purified Krill oil) for dogs with arthritis, hip dysplasia, or senior joint stiffness.",
            keyBenefits = listOf("Clinically proven mobility improvement", "Soothes stiff joints and limping", "Rich in Omega 3 EFA"),
            dosageOrUsage = "1 tablet daily with food"
        )
    )

    fun getGroomingServices(): List<GroomingServiceItem> = listOf(
        GroomingServiceItem(
            id = "groom_1",
            title = "Complete Kerala In-Home Spa Van Service",
            subTitle = "Air-conditioned grooming van at your doorstep in Kochi, TVM, Kozhikode & Thrissur",
            isInHomeVan = true,
            durationMinutes = 90,
            priceInr = 1499.0,
            originalPriceInr = 1999.0,
            perks = listOf(
                "Warm Hydrobath with Organic Shampoo",
                "Blow-dry & Deep De-shedding",
                "Sanitary Trim & Paw Pad Shaving",
                "Ear Cleaning & Eye Stain Removal",
                "Nail Clipping & Filing",
                "Anti-tick Neem Mist & Pet Cologne"
            ),
            description = "Our fully equipped luxury pet grooming van arrives at your home address. Zero stress for your pet, no travel anxiety."
        ),
        GroomingServiceItem(
            id = "groom_2",
            title = "Medicated Ayurvedic Neem Tick & Flea Bath",
            subTitle = "Herbal soothing bath to eliminate parasites & soothe Kerala humid itch",
            isInHomeVan = true,
            durationMinutes = 60,
            priceInr = 899.0,
            originalPriceInr = 1199.0,
            perks = listOf(
                "Ayurvedic Neem & Tea Tree Medicated Soak",
                "Flea Comb Out & Egg Eradication",
                "Soothing Aloe Vera Skin Balm",
                "Blow Dry & Brush Out",
                "Paw Moisturizing Treatment"
            ),
            description = "Specialized for Kerala weather to tackle stubborn tick infestations and allergic dermatitis."
        ),
        GroomingServiceItem(
            id = "groom_3",
            title = "Full Breed Haircut & Style Grooming",
            subTitle = "Expert breed-specific styling for Shih Tzus, Poodles, Golden Retrievers & Persians",
            isInHomeVan = false,
            durationMinutes = 120,
            priceInr = 1299.0,
            originalPriceInr = 1600.0,
            perks = listOf(
                "Full Body Breed Standard Haircut",
                "Teddy Bear Face or Summer Cut",
                "Double Bath & Silk Conditioner",
                "Teeth Foam Cleanse & Breath Spray",
                "Complimentary Cute Bandana"
            ),
            description = "Master groomers style your pet to show or summer perfection. Available in-clinic or in our mobile grooming van."
        ),
        GroomingServiceItem(
            id = "groom_4",
            title = "Express Quick Hygiene Spa Session",
            subTitle = "Essential quick clean for everyday maintenance",
            isInHomeVan = true,
            durationMinutes = 45,
            priceInr = 699.0,
            originalPriceInr = 850.0,
            perks = listOf(
                "Nail Clipping & Paw Trimming",
                "Ear Flush & Cleaning",
                "Sanitary Area Clean & Trim",
                "Deodorizing Dry Mist & Brushing"
            ),
            description = "Quick 45-minute hygiene touch-up to keep your pet clean and comfortable between deep baths."
        )
    )

    fun getVerifiedDoctors(): List<VerifiedDoctor> = listOf(
        VerifiedDoctor(
            id = "vet_1",
            name = "Dr. Anjali Menon",
            degrees = "BVSc & AH, MVSc (Veterinary Surgery)",
            ksvcRegNumber = "KSVC/2017/4821",
            specialization = "Senior Small Animal Surgeon & Orthopedic Specialist",
            experienceYears = 11,
            clinicName = "Cochin Pet Specialty Hospital",
            clinicCity = "Kochi",
            clinicAddress = "Panampilly Nagar Main Ave, Kochi, Kerala 682036",
            videoConsultFeeInr = 399.0,
            inPersonConsultFeeInr = 599.0,
            rating = 4.9,
            reviewsCount = 342,
            availableDays = "Mon - Sat (9:30 AM - 7:00 PM)",
            phone = "+91 98470 44921",
            isEmergencyAvailable = true
        ),
        VerifiedDoctor(
            id = "vet_2",
            name = "Dr. Rahul Varma",
            degrees = "BVSc & AH, MVSc (Internal Medicine)",
            ksvcRegNumber = "KSVC/2015/3912",
            specialization = "Canine & Feline Medicine Specialist",
            experienceYears = 14,
            clinicName = "Travancore Animal Poly Clinic",
            clinicCity = "Trivandrum",
            clinicAddress = "Kowdiar Junction, Near Golf Club, Thiruvananthapuram 695003",
            videoConsultFeeInr = 349.0,
            inPersonConsultFeeInr = 549.0,
            rating = 4.9,
            reviewsCount = 288,
            availableDays = "Mon - Sun (10:00 AM - 8:00 PM)",
            phone = "+91 94471 66320",
            isEmergencyAvailable = true
        ),
        VerifiedDoctor(
            id = "vet_3",
            name = "Dr. Harish K. Nair",
            degrees = "BVSc & AH, PG Dip Dermatology",
            ksvcRegNumber = "KSVC/2019/5104",
            specialization = "Pet Dermatology & Allergy Specialist",
            experienceYears = 8,
            clinicName = "Malabar Veterinary Care Center",
            clinicCity = "Kozhikode",
            clinicAddress = "Mavoor Road, Arayidathupalam, Kozhikode 673004",
            videoConsultFeeInr = 299.0,
            inPersonConsultFeeInr = 499.0,
            rating = 4.8,
            reviewsCount = 195,
            availableDays = "Tue - Sun (9:00 AM - 6:00 PM)",
            phone = "+91 98462 88102",
            isEmergencyAvailable = false
        ),
        VerifiedDoctor(
            id = "vet_4",
            name = "Dr. Deepa Thomas",
            degrees = "BVSc & AH, MVSc (Avian & Exotic Animals)",
            ksvcRegNumber = "KSVC/2018/6239",
            specialization = "Avian, Reptile & Exotic Pet Specialist",
            experienceYears = 9,
            clinicName = "Thrissur Exotic & Small Animal Clinic",
            clinicCity = "Thrissur",
            clinicAddress = "Swaraj Round North, Near Town Hall, Thrissur 680020",
            videoConsultFeeInr = 449.0,
            inPersonConsultFeeInr = 699.0,
            rating = 4.9,
            reviewsCount = 160,
            availableDays = "Mon - Fri (10:00 AM - 5:30 PM)",
            phone = "+91 94460 77319",
            isEmergencyAvailable = true
        ),
        VerifiedDoctor(
            id = "vet_5",
            name = "Dr. Sreeram Nambiar",
            degrees = "BVSc & AH (Kerala Vet University)",
            ksvcRegNumber = "KSVC/2021/7190",
            specialization = "General Veterinary Physician & Emergency Care",
            experienceYears = 6,
            clinicName = "Kerala 24/7 Mobile Vet Squad",
            clinicCity = "Kochi",
            clinicAddress = "Kakkanad InfoPark Express Road, Kochi 682030",
            videoConsultFeeInr = 249.0,
            inPersonConsultFeeInr = 449.0,
            rating = 4.8,
            reviewsCount = 120,
            availableDays = "24/7 On-Call & Video Consults",
            phone = "+91 98471 11099",
            isEmergencyAvailable = true
        )
    )

    fun getInitialEscrowOrders(): List<EscrowOrder> = listOf(
        EscrowOrder(
            orderId = "ORD-KL-88392",
            items = listOf(
                CartItem(
                    id = "c1",
                    itemId = "food_1",
                    title = "Royal Canin Maxi Puppy Dry Dog Food",
                    subtitle = "4 kg Pack",
                    priceInr = 2950.0,
                    quantity = 1
                ),
                CartItem(
                    id = "c2",
                    itemId = "med_1",
                    title = "Bravecto Chewable Tablet (10-20 kg)",
                    subtitle = "12-Week Tick & Flea Protection",
                    priceInr = 1850.0,
                    quantity = 1
                )
            ),
            subtotalInr = 4800.0,
            deliveryFeeInr = 0.0, // Free delivery above 499
            ecoPackagingFeeInr = 10.0,
            totalInr = 4810.0,
            deliveryCity = "Kochi",
            deliveryAddress = "Flat 4B, Olive Heights, Panampilly Nagar, Kochi 682036",
            customerName = "Alex Morgan",
            customerPhone = "+91 98470 12345",
            paymentMethod = "Cash on Delivery (COD)",
            isEscrowProtected = true,
            status = OrderStatus.OUT_FOR_DELIVERY,
            deliveryOtp = "7392",
            deliveryRiderName = "Sreejith K. (Kochi Hub)",
            deliveryRiderVehicle = "KL-07-CB-4412",
            deliveryRiderPhone = "+91 98471 99221",
            orderDate = "Today, 11:15 AM",
            timeline = listOf(
                OrderTimelineEvent(
                    title = "Order Placed & Escrow Secured",
                    description = "₹4,810 held securely in Jane & Pals Escrow vault.",
                    timestamp = "11:15 AM",
                    isCompleted = true
                ),
                OrderTimelineEvent(
                    title = "Confirmed by Kochi Pharmacy Hub",
                    description = "Inventory verified & batch expiry checked (Exp: Oct 2027).",
                    timestamp = "11:22 AM",
                    isCompleted = true
                ),
                OrderTimelineEvent(
                    title = "Packed in Eco-Insulated Box",
                    description = "Dispatched from Ernakulam Central Logistics Center.",
                    timestamp = "11:45 AM",
                    isCompleted = true
                ),
                OrderTimelineEvent(
                    title = "Out for Doorstep Delivery",
                    description = "Rider Sreejith K. is on the way (KL-07-CB-4412). Share OTP 7392 on arrival.",
                    timestamp = "12:10 PM",
                    isCompleted = true,
                    isCurrent = true
                ),
                OrderTimelineEvent(
                    title = "Delivered & Escrow Released",
                    description = "Funds will only be released to merchant after OTP verification.",
                    timestamp = "Estimated 12:45 PM",
                    isCompleted = false
                )
            )
        )
    )

    fun getInitialDoctorBookings(): List<DoctorBooking> = listOf(
        DoctorBooking(
            bookingId = "BKG-VET-4410",
            doctorName = "Dr. Anjali Menon",
            doctorSpecialization = "Senior Small Animal Surgeon",
            clinicName = "Cochin Pet Specialty Hospital",
            city = "Kochi",
            consultType = "Video Consultation",
            petName = "Jane",
            date = "Tomorrow",
            timeSlot = "04:30 PM - 05:00 PM",
            feeInr = 399.0,
            status = "Confirmed",
            meetingLinkOrAddress = "https://meet.google.com/jp-vet-kerala"
        )
    )
}
